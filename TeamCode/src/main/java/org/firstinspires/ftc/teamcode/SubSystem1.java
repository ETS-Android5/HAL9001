package org.firstinspires.ftc.teamcode;

import com.SCHSRobotics.HAL9001.system.config.ConfigData;
import com.SCHSRobotics.HAL9001.system.config.ConfigParam;
import com.SCHSRobotics.HAL9001.system.config.TeleopConfig;
import com.SCHSRobotics.HAL9001.system.robot.Robot;
import com.SCHSRobotics.HAL9001.system.robot.SubSystem;
import com.SCHSRobotics.HAL9001.util.control.Button;
import com.SCHSRobotics.HAL9001.util.control.CustomizableGamepad;

public class SubSystem1 extends SubSystem {

    enum TESTING {
        A, B, C, D
    }
    TESTING test;

    public SubSystem1(Robot robot) {
        super(robot);
        usesConfig = true;
        CustomizableGamepad g = new CustomizableGamepad(robot);
        g.addButton("", new Button<Boolean>(1, Button.BooleanInputs.a));
    }

    @Override
    public void init() {

    }

    @Override
    public void init_loop() {

    }

    @Override
    public void start() {
        ConfigData data = robot.pullNonGamepad(this);
        test = data.getData("TEST", TESTING.class);
    }

    @Override
    public void handle(){

    }

    @Override
    public void stop() {

    }

    @TeleopConfig
    public static ConfigParam[] teleopConfig() {
        return new ConfigParam[] {
                new ConfigParam("TEST", TESTING.A),
                new ConfigParam("TEST2", TESTING.A),
                new ConfigParam("TEST3", TESTING.A)
        };
    }
}
