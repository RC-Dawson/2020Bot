/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;

import frc.robot.subsystems.Storage;
import frc.robot.subsystems.Intake;

public class ClearBalls extends CommandBase {
  private static Storage m_storage = new Storage();
  private static Intake m_intake = new Intake();
  
  /**
   * Creates a new ClearBalls.
   */
  public ClearBalls(Storage storage, Intake intake) {
    m_intake = intake;
    m_storage = storage;

    addRequirements(m_intake, m_storage);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_intake.open();
    m_intake.reverse();
    m_storage.reverse();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_intake.retract();
    m_intake.stopRunning();
    m_storage.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
