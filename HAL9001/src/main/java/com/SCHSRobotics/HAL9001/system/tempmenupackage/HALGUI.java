package com.SCHSRobotics.HAL9001.system.tempmenupackage;

import com.SCHSRobotics.HAL9001.system.source.BaseRobot.Robot;
import com.SCHSRobotics.HAL9001.util.exceptions.DumpsterFireException;
import com.SCHSRobotics.HAL9001.util.exceptions.ExceptionChecker;
import com.SCHSRobotics.HAL9001.util.misc.Button;
import com.SCHSRobotics.HAL9001.util.misc.CustomizableGamepad;
import com.SCHSRobotics.HAL9001.util.misc.Toggle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A GUI class to control the menu system.
 * This class follows a singleton pattern.
 *
 * @author Cole Savage, Level Up
 * @since 1.1.0
 * @version 1.1.0
 *
 * Creation Date: 4/29/20
 *
 * @see HALMenu
 */
public class HALGUI {
    //A queue to store all currently active trees of menus.
    private Queue<Stack<HALMenu>> menuStacks;
    //A queue to store all controls for the cursor. Controls apply to each menu tree. Controls default to the dpad.
    private Queue<EntireViewButton> cursorControlQueue;
    //The current menu stack. Shows history of menus visited within a tree. Does not contain currently active menu.
    private Stack<HALMenu> currentStack;
    //Stores backups of "undo-ed" menus in a tree so that they can be "redo-ed".
    private Stack<HALMenu> forwardStack;
    //The currently selected menu
    private HALMenu currentMenu;
    //The robot the gui uses to send messages to telemetry.
    private Robot robot;
    //The last millisecond timestamp of when the render function was called.
    private long lastRenderTime;
    //The customizable gamepad used to cycle between menu keys.
    private CustomizableGamepad cycleControls;
    //The button used to cycle between menu keys.
    private Button<Boolean> cycleButton;
    //The toggle object used to correctly track button presses.
    private Toggle cycleToggle;
    //The single static instance of the gui.
    private static HALGUI INSTANCE = new HALGUI();

    private boolean justInflated;
    private long inflationTimeMs;
    private static final int DEFAULT_TRANSMISSION_INTERVAL_MS = 250;
    private static final int DEFAULT_HAL_TRANSMISSION_INTERVAL_MS = 50;
    private static final long POST_INFLATION_WAIT_TIME = 250;

    /**
     * The private GUI constructor. Initializes the queues, the render timestamp, and the cycle toggle.
     */
    private HALGUI() {}

    /**
     * Gets the static instance of the gui.
     *
     * @return The static instance of the gui.
     */
    @Contract(pure = true)
    public static HALGUI getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the GUI with a robot and menu-cycling button.
     *
     * @param robot The robot that the gui will use to control the telemetry.
     * @param cycleButton The button used to cycle between menu trees.
     */
    public void setup(Robot robot, Button<Boolean> cycleButton) {
        this.robot = robot;
        cycleControls = new CustomizableGamepad(robot);
        this.cycleButton = cycleButton;
        menuStacks = new LinkedBlockingQueue<>();
        cursorControlQueue = new LinkedBlockingQueue<>();
        forwardStack = new Stack<>();
        lastRenderTime = 0;
        cycleToggle = new Toggle(Toggle.ToggleTypes.trueOnceToggle, false);
        justInflated = false;
        inflationTimeMs = 0;
        robot.telemetry.setMsTransmissionInterval(DEFAULT_HAL_TRANSMISSION_INTERVAL_MS);
    }

    /**
     * Gets the robot the gui is using to control the telemetry.
     *
     * @return The robot that the gui will use to control the telemetry.
     */
    public Robot getRobot() {
        return robot;
    }

    /**
     * Adds a menu at the root of a new menu tree. This creates a new menu tree.
     *
     * @param menu The root menu of the new tree.
     */
    public void addRootMenu(@NotNull HALMenu menu) {
        currentMenu = menu;
        currentStack = new Stack<>();
        forwardStack.clear();
        currentStack.push(currentMenu);
        menuStacks.add(currentStack);
        cursorControlQueue.add(new EntireViewButton()
                .onClick(new Button<>(1, Button.BooleanInputs.dpad_up), () -> currentMenu.cursorUp())
                .onClick(new Button<>(1, Button.BooleanInputs.dpad_down), () -> currentMenu.cursorDown())
                .onClick(new Button<>(1, Button.BooleanInputs.dpad_left), () -> currentMenu.cursorLeft())
                .onClick(new Button<>(1, Button.BooleanInputs.dpad_right), () -> currentMenu.cursorRight()));
        currentMenu.addItem(cursorControlQueue.peek());
        currentMenu.init(new Payload());
    }

    /**
     * Adds a new menu to the current tree and displays it. This does not create a new tree, and instead adds on to the existing tree.
     *
     * @param menu The menu to add to the new tree and display.
     */
    public void inflate(@NotNull HALMenu menu) {
        currentStack.push(currentMenu);
        forwardStack.clear();
        currentMenu = menu;
        currentMenu.addItem(cursorControlQueue.peek());
        currentMenu.init(currentMenu.payload);
        currentMenu.disableListeners(POST_INFLATION_WAIT_TIME);
    }

    /**
     * Renders the currently active menu.
     */
    public void renderCurrentMenu() {
        boolean forceCursorUpdate = false;
        if(!menuStacks.isEmpty()) {
            forceCursorUpdate = currentMenu.updateListeners();
        }

        if(!menuStacks.isEmpty() && System.currentTimeMillis() - lastRenderTime >= currentMenu.getCursorBlinkSpeedMs() || forceCursorUpdate) {
            currentMenu.notifyForceCursorUpdate(forceCursorUpdate);
            currentMenu.render();
            robot.telemetry.update();
            lastRenderTime = System.currentTimeMillis();
        }

        cycleToggle.updateToggle(cycleControls.getInput(cycleButton));
        if(cycleToggle.getCurrentState() && !menuStacks.isEmpty()) {
            menuStacks.add(menuStacks.poll());
            currentStack = menuStacks.peek();
            ExceptionChecker.assertNonNull(currentStack, new DumpsterFireException("The stack of menus that you tried to switch to is null. This state should be unreachable, what did you do?!?"));
            currentMenu = currentStack.peek();
            cursorControlQueue.add(cursorControlQueue.poll());
            forwardStack.clear();
        }
    }

    public void setCursorControls(Button<Boolean> upButton, Button<Boolean> downButton, Button<Boolean> leftButton, Button<Boolean> rightButton) {
        ExceptionChecker.assertFalse(upButton.equals(downButton) ||
                upButton.equals(leftButton) ||
                upButton.equals(rightButton) ||
                downButton.equals(leftButton) ||
                downButton.equals(rightButton) ||
                leftButton.equals(rightButton), new DumpsterFireException("All cursor controls must be unique"));

        EntireViewButton newCursor = new EntireViewButton()
                .onClick(upButton, () -> currentMenu.cursorUp())
                .onClick(downButton, () -> currentMenu.cursorDown())
                .onClick(leftButton, () -> currentMenu.cursorLeft())
                .onClick(rightButton, () -> currentMenu.cursorRight());

        cursorControlQueue.poll();
        cursorControlQueue.add(newCursor);
        for (int i = 0; i < cursorControlQueue.size() - 1; i++) {
            cursorControlQueue.add(cursorControlQueue.poll());
        }

        currentMenu.setCursor(newCursor);
    }

    public void back(@NotNull Payload payload) {
        if(currentStack.size() > 1) {
            forwardStack.push(currentStack.pop());
            currentMenu = currentStack.peek();
            currentMenu.addItem(cursorControlQueue.peek());
            currentMenu.init(payload);
            currentMenu.disableListeners(POST_INFLATION_WAIT_TIME);
        }
    }

    public void back() {
        back(new Payload());
    }

    public void forward(@NotNull Payload payload) {
        if(!forwardStack.isEmpty()) {
            currentStack.push(forwardStack.pop());
            currentMenu = currentStack.peek();
            currentMenu.init(payload);
        }
    }

    public void forward() {
        forward(new Payload());
    }

    public void stop() {
        robot.telemetry.setMsTransmissionInterval(DEFAULT_TRANSMISSION_INTERVAL_MS);
        currentStack = null;
        currentMenu = null;
        robot = null;
        cycleControls = null;
        cycleButton = null;
    }
}