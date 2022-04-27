package com.manenkov.flow

case class Configuration(
                          dailyLimit: Option[Long] = None,
                          weeklyLimit: Option[Long] = None,
                          monthlyLimit: Option[Long] = None,
                          mondayLimit: Option[Long] = None,
                          tuesdayLimit: Option[Long] = None,
                          wednesdayLimit: Option[Long] = None,
                          thursdayLimit: Option[Long] = None,
                          fridayLimit: Option[Long] = None,
                          saturdayLimit: Option[Long] = None,
                          sundayLimit: Option[Long] = None,
                        )
