// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

#pragma once

#include <wpi/SymbolExports.h>

#include "frc/geometry/Twist2d.h"
#include "frc/kinematics/ChassisSpeeds.h"

namespace frc {
/**
 * Helper class that converts a chassis velocity (dx, dy, and dtheta components)
 * into individual wheel speeds. Robot code should not use this directly-
 * Instead, use the particular type for your drivetrain (e.g.,
 * DifferentialDriveKinematics).
 *
 * Inverse kinematics converts a desired chassis speed into wheel speeds whereas
 * forward kinematics converts wheel speeds into chassis speed.
 */
template <typename WheelSpeeds, typename WheelPositions>
class WPILIB_DLLEXPORT Kinematics {
 public:
  /**
   * Performs forward kinematics to return the resulting chassis speed from the
   * wheel speeds. This method is often used for odometry -- determining the
   * robot's position on the field using data from the real-world speed of each
   * wheel on the robot.
   *
   * @param wheelSpeeds The speeds of the wheels.
   * @return The chassis speed.
   */
  virtual ChassisSpeeds ToChassisSpeeds(
      const WheelSpeeds& wheelSpeeds) const = 0;

  /**
   * Performs inverse kinematics to return the wheel speeds from a desired
   * chassis velocity. This method is often used to convert joystick values into
   * wheel speeds.
   *
   * @param chassisSpeeds The desired chassis speed.
   * @return The wheel speeds.
   */
  virtual WheelSpeeds ToWheelSpeeds(
      const ChassisSpeeds& chassisSpeeds) const = 0;

  /**
   * Performs forward kinematics to return the resulting Twist2d from the given
   * wheel deltas. This method is often used for odometry -- determining the
   * robot's position on the field using changes in the distance driven by each
   * wheel on the robot.
   *
   * @param wheelDeltas The distances driven by each wheel.
   *
   * @return The resulting Twist2d in the robot's movement.
   */
  virtual Twist2d ToTwist2d(const WheelPositions& wheelDeltas) const = 0;
};
}  // namespace frc
