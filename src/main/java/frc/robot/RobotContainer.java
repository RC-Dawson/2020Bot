/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import frc.robot.Constants.OIConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.LEDStrip;
import frc.robot.subsystems.Storage;
import frc.robot.subsystems.WheelSpinner;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Limelight;
import frc.robot.commands.AlignWithVision;
import frc.robot.commands.Auto1;
import frc.robot.commands.Auto2;
import frc.robot.commands.AutoPID;
import frc.robot.commands.IndexBalls;
import frc.robot.commands.IndexOrReverse;
import frc.robot.commands.ExhaustBalls;
import frc.robot.commands.Stage1Spin;

import static edu.wpi.first.wpilibj.XboxController.Button;

import java.util.List;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  public final Drivetrain m_drivetrain = new Drivetrain();
  private final Intake m_intake = new Intake();
  private final Shooter m_shooter = new Shooter();
  private final Storage m_storage = new Storage();
  private final Climber m_climber = new Climber();
  private final WheelSpinner m_spinner = new WheelSpinner();

  private final Limelight m_limelight = new Limelight();

  public final LEDStrip m_ledStrip = new LEDStrip();

  // A chooser for autonomous commands
  SendableChooser<Command> m_chooser = new SendableChooser<>();

  //the sendable auto routines to chose from
  private final CommandBase m_auto1 = new Auto1(m_drivetrain, m_shooter, m_storage, 4096);
  private final CommandBase m_auto2 = new Auto2(m_drivetrain);


  private double[] blue = new double[]{0,0,255};
  private double[] white = new double[]{100,100,100};

  // The driver's controller
  public XboxController m_driverController = new XboxController(OIConstants.kDriverControllerPort);
    

  /**
   * The container for the robot.  Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {

    // Configure the button bindings
    configureButtonBindings();

    // Configure default commands
    // Set the default drive command to split-stick arcade drive
    m_drivetrain.setDefaultCommand(
        // A split-stick arcade command, with forward/backward controlled by the left
        // hand, and turning controlled by the right.
        new RunCommand(() -> m_drivetrain
            .arcadeDrive(DriveConstants.kDriveCoefficient * m_driverController.getRawAxis(1),
                         DriveConstants.kTurnCoefficient * m_driverController.getRawAxis(4)), m_drivetrain));

    //make the bumpers control the bar side to side motors.
    // m_climber.setDefaultCommand(
    //   new RunCommand(
    //         () -> m_climber
    //     .driveOnBar(m_driverController.getRawAxis(3), m_driverController.getRawAxis(4))
    // ));


    // m_limelight.setDefaultCommand(
    //   new RunCommand(() -> m_limelight.update(true)) //makes the limelight update to the smartdashboard constantly
    // );

    m_storage.setDefaultCommand(
      new IndexOrReverse(m_intake, m_storage, m_driverController.getRawAxis(4))
      //new RunCommand(m_storage::stop, m_storage)
    );
    
    // m_spinner.setDefaultCommand(
    //   new RunCommand(m_spinner::getColor, m_spinner)
    // );

    // Add commands to the autonomous command chooser
    m_chooser.addOption("backwards and shoot", m_auto1);
    m_chooser.addOption("forwards off line", m_auto2);
    // Put the chooser on the dashboard
    Shuffleboard.getTab("Autonomous").add(m_chooser);

  }

  /**
   * Use this method to define your button->command mappings.  Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
   * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    // Pop intake out when the right bumper is pressed.
    new JoystickButton(m_driverController, Button.kBumperRight.value)
      .whenPressed( new SequentialCommandGroup(
          new InstantCommand(m_intake::open, m_intake),
          new InstantCommand(m_intake::runNow, m_intake),
          new IndexBalls(m_storage)
      )
    );

    // bring intake in when button is released
    new JoystickButton(m_driverController, Button.kBumperLeft.value).whenPressed(
      new InstantCommand(m_intake::stopRunning, m_intake).andThen(
      new InstantCommand(m_intake::retract, m_intake),
      new InstantCommand(m_storage::stop, m_storage))
    );

    //feed balls while the x is held
    new JoystickButton(m_driverController, Button.kX.value).whileHeld(
          new InstantCommand(m_shooter::runOpenLoop, m_shooter).andThen(
            new InstantCommand(() -> m_ledStrip.setColor(blue)),
            new InstantCommand(m_shooter::runOpenLoop, m_shooter))
            //new InstantCommand(() -> m_limelight.setVisionMode(1)),
            //new AlignWithVision(m_drivetrain, m_limelight))
            //new ExhaustBalls(m_storage, m_shooter)v
    );

    //stop feeding when x is released
    new JoystickButton(m_driverController, Button.kX.value).whenReleased(
          new InstantCommand(m_shooter::stopShooter, m_shooter).andThen(
            new InstantCommand(() -> m_ledStrip.setColor(white)),
            new InstantCommand(() -> m_limelight.setVisionMode(0)),
            new InstantCommand(m_storage::stop, m_storage))
    );

    //push balls away while the left stick is pressed
    new JoystickButton(m_driverController, Button.kStickLeft.value)
      .whenPressed(
        new ParallelCommandGroup(
          new RunCommand(() -> m_intake.reverse(m_driverController.getRawAxis(2)), m_intake),
          new RunCommand(() -> m_storage.reverse(m_driverController.getRawAxis(2)), m_storage)
        )
    );

    //play music while back is held :)
    // new JoystickButton(m_driverController, Button.kBack.value).whileHeld(new RunCommand(m_drivetrain::playMusic, m_drivetrain));

    //open climber while back is held
    // new JoystickButton(m_driverController, Button.kBack.value).whileHeld(new RunCommand(m_climber::extendPiston, m_climber));
    // new JoystickButton(m_driverController, Button.kBack.value).whenReleased(new InstantCommand(m_climber::retractPiston, m_climber));


    //open wheel spinner and run while 'B' is HELD
    new JoystickButton(m_driverController, Button.kB.value).whileHeld(
        new RunCommand(m_spinner::retract, m_spinner)
    );

    new JoystickButton(m_driverController, Button.kB.value).whenReleased(
        new InstantCommand(m_spinner::extend, m_spinner).andThen(
        new InstantCommand(m_spinner::run, m_spinner))
    );

    new JoystickButton(m_driverController, Button.kB.value).whenReleased(
        new InstantCommand(m_spinner::stop, m_spinner).andThen(
        new InstantCommand(m_spinner::retract, m_spinner))
    );

    //swap button for driving
    new JoystickButton(m_driverController, Button.kY.value).whenPressed(
      new InstantCommand(() -> m_drivetrain.toggleSwap(), m_drivetrain)
    );
    

    //extend climber when start is pressed
      new JoystickButton(m_driverController, Button.kStart.value).whileHeld(
        new InstantCommand(m_climber::extendClimber, m_climber)
    );

    new JoystickButton(m_driverController, Button.kStart.value).whenReleased(
      new InstantCommand(m_climber::stop, m_climber)
    );

    new JoystickButton(m_driverController, Button.kBack.value).whileHeld(
      new RunCommand(m_climber::reverseWinch, m_climber)
    );

    new JoystickButton(m_driverController, Button.kBack.value).whenReleased(
      new InstantCommand(m_climber::stopWinch, m_climber)
    );

    new JoystickButton(m_driverController, Button.kB.value).whenPressed(
      new InstantCommand(m_climber::extendPiston, m_climber)
    );

    new JoystickButton(m_driverController, Button.kStickRight.value).whenPressed(
      new SequentialCommandGroup(
        new InstantCommand(m_climber::retractPiston, m_climber),
        new InstantCommand(m_climber::reverse, m_climber)
      )
    );
    new JoystickButton(m_driverController, Button.kStickRight.value).whenReleased(
      new InstantCommand(m_climber::stop, m_climber)
    );

    new JoystickButton(m_driverController, Button.kA.value).whileHeld(
      new InstantCommand(m_spinner::extend, m_spinner).andThen(
      new InstantCommand(m_spinner::run))
    );

    new JoystickButton(m_driverController, Button.kA.value).whenReleased(
      new InstantCommand(m_spinner::retract, m_spinner).andThen(
      new InstantCommand(m_spinner::stop))
    );
  
    //upper dpad button for transport testing. runs transport when pressed
    new POVButton(m_driverController, 90).whenPressed(
      new RunCommand(m_storage::run, m_storage)
    );

    //stops transport when released
    new POVButton(m_driverController, 90).whenReleased(
      new InstantCommand(m_storage::stop, m_storage)
    );

    //stops intake but keeps it down
    // new POVButton(m_driverController , 270).whenPressed(
    //   new InstantCommand(m_intake::stopRunning, m_intake)
    // );
    new POVButton(m_driverController , 270).whileHeld(
      new RunCommand(m_climber::winch, m_climber)
    );
    new POVButton(m_driverController , 270).whenReleased(
      new InstantCommand(m_climber::stopWinch, m_climber)
    );

    //increases shooter speed
    new POVButton(m_driverController, 0).whenPressed(
      new InstantCommand(m_shooter::increaseShooterSpeed, m_shooter)
    );

    //decreases shooter speed
    new POVButton(m_driverController, 180).whenPressed(
      new InstantCommand(m_shooter::decreaseShooterSpeed, m_shooter)
    );
  }


  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return m_chooser.getSelected();
  }

}
