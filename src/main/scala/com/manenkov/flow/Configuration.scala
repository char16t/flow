package com.manenkov.flow

case class Configuration(
                          dailyLimit: Option[Long] = None,
                          weeklyLimit: Option[Long] = None,
                          monthlyLimit: Option[Long] = None,
                        )
