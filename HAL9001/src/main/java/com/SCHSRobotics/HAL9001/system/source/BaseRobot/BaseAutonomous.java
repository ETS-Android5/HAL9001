/*
 * Filename: BaseAutonomous.java
 * Author: Andrew Liang
 * Team Name: Level Up
 * Date: 2017
 */

package com.SCHSRobotics.HAL9001.system.source.BaseRobot;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Supplier;

/**
 * An abstract class used to more easily create opmodes.
 */
public abstract class BaseAutonomous extends LinearOpMode {

    //The robot running the opmode.
    private Robot robot;

    /**
     * An abstract method that is used to instantiate the robot.
     *
     * @return The robot being used in the opmode.
     */
    protected abstract Robot buildRobot();

    /**
     * Method that runs when the robot is initialized. It is not an abstract method so that it does not have to be implemented if it
     * is unneeded.
     */
    protected void onInit() {}

    /**
     * Method that runs in a loop after the robot is initialized. It is not an abstract method so that it does not have to be implemented if it
     * is unneeded.
     */
    protected void onInitLoop() {}


    /**
     * Method that runs when the robot is stopped. It is not an abstract method so that it does not have to be implemented if it
     * is unneeded.
     */
    protected void onStop() {}

    /**
     * An abstract method that contains the code for the robot to run.
     */
    public abstract void main();

    @Override
    public final void runOpMode() {
        robot = buildRobot();

        try {
            robot.init();
            onInit();
            while(!isStarted() && !isStopRequested()) {
                robot.init_loop();
                onInitLoop();
            }
        } catch (Exception ex) {
            telemetry.clearAll();
            telemetry.addData("ERROR!!!", ex.getMessage());
            telemetry.update();
            Log.e(this.getClass().getSimpleName(), ex.getMessage(), ex);
        }

        if(!isStopRequested()) {
            try {
                robot.onStart();
                main();
            } catch (Exception ex) {
                telemetry.clearAll();
                telemetry.addData("ERROR!", ex.getMessage());
                telemetry.update();
                Log.e(this.getClass().getSimpleName(), ex.getMessage(), ex);
            }
        }

        onStop();
        robot.stopAllComponents();
    }

    /**
     * Gets the robot running the program.
     *
     * @return The robot running this program.
     */
    protected final Robot getRobot() {
        return robot;
    }

    /**
     * Waits for a specified number of milliseconds.
     *
     * @param millis The number of milliseconds to wait.
     */
    protected final void waitTime(long millis) {
        long stopTime = System.currentTimeMillis() + millis;
        while (robot.opModeIsActive() && System.currentTimeMillis() < stopTime) {
            sleep(1);
        }
    }

    /**
     * Waits for a specified number of milliseconds, running a function in a loop while its waiting.
     *
     * @param millis The number of milliseconds to wait.
     * @param runner The code to run each loop while waiting.
     */
    protected final void waitTime(long millis, Runnable runner) {
        long stopTime = System.currentTimeMillis() + millis;
        while (robot.opModeIsActive() && System.currentTimeMillis() < stopTime) {
            runner.run();
            sleep(1);
        }
    }

    /**
     * Waits until a condition returns true.
     *
     * @param condition The boolean condition that must be true in order for the program to stop waiting.
     */
    protected final void waitUntil(Supplier<Boolean> condition) {
        while (robot.opModeIsActive() && !condition.get()) {
            sleep(1);
        }
    }

    /**
     * Waits until a condition returns true, running a function in a loop while its waiting.
     *
     * @param condition The boolean condition that must be true in order for the program to stop waiting.
     * @param runner The code to run each loop while waiting.
     */
    protected final void waitUntil(Supplier<Boolean> condition, Runnable runner) {
        while (robot.opModeIsActive() && !condition.get()) {
            runner.run();
            sleep(1);
        }
    }

    /**
     * Waits while a condition is true.
     *
     * @param condition The boolean condition that must become false for the program to stop waiting.
     */
    protected final void waitWhile(Supplier<Boolean> condition) {
        while (robot.opModeIsActive() && condition.get()) {
            sleep(1);
        }
    }

    /**
     * Waits while a condition is true, running a function in a loop while its waiting.
     *
     * @param condition The boolean condition that must become false for the program to stop waiting.
     * @param runner The code to run each loop while waiting.
     */
    protected final void waitWhile(Supplier<Boolean> condition, Runnable runner) {
        while (robot.opModeIsActive() && condition.get()) {
            runner.run();
            sleep(1);
        }
    }

    /**
     * Waits a certain amount of time.
     *
     * @param millis The amount of time in milliseconds to wait.
     * @deprecated Renamed to waitTime
     */
    @Deprecated
    protected final void waitFor(long millis) {
        waitTime(millis);
    }
}
