/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Storage;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class Auto1 extends SequentialCommandGroup {
  
  /**
   * Creates a new Auto1.
   */
  public Auto1(Drivetrain drivetrain, Shooter shooter, Storage storage) {
    addCommands(
      new DriveStraightPID(5, drivetrain),
      new InstantCommand(shooter::runOpenLoop, shooter),
      new ExhaustBalls(storage, shooter).withTimeout(10)
    );
  }
}
