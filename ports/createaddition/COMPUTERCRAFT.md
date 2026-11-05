# Create Crafts & Additions: Computercraft Peripheral API

Version 1.1

Supported Blocks:
- [Electric Motor](#electric-motor)
- [Servo Motor](#servo-motor)
- [Accumulator](#accumulator)
- [Portable Energy Interface](#portable-energy-interface-pei)
- [Redstone Relay](#redstone-relay)
- [Digital Adapter](#digital-adapter)
  - [Rotational Speed Controller](#rotational-speed-controller)
  - [Stressometer](#stressometer)
  - [Speedometer](#speedometer)
  - [Rope, Hose, and Elevator Pulley](#pulleys)
  - [Elevator Pulley](#elevator-pulley)
  - [Mechanical Piston](#mechanical-piston)
  - [Mechanical Bearing](#mechanical-bearing)
  - [Display Link](#display-link)
  - [Other](#other)

## Other Languages
 [简体中文 (Simplified Chinese)](COMPUTERCRAFT_zh-CN.md)

# Electric Motor
To set the speed of the Electric Motor, call `setSpeed(rpm)` where the argument *rpm* is a number between `-256` and `256`. The function will throw an exception if it is called too many times per second.
```lua
motor.setSpeed(rpm)
```
The function `stop()` is a shorthand for `setSpeed(0)`.
```lua
motor.stop()
```
In the following example, the motor attached to the left of the computer will rotate at 32RPM for 5 seconds and then stop.
```lua
local motor = peripheral.wrap("left")
motor.setSpeed(32)
sleep(5)
motor.stop()
```

The function `rotate(degrees, [rpm])` will return the time it will take to rotate the shaft by the argument *degrees* at the current speed. If the optional argument *rpm* is given it will set the speed of the motor and return the rotation time at the new speed.
```lua
motor.setSpeed(32)
sleep(motor.rotate(90))
motor.stop()
```
In the following example, the motor will first rotate 180 degrees in the clockwise direction, then 180 degrees at half the speed in the anti-clockwise direction and then finaly stop.
```lua
local motor = peripheral.wrap("left")
sleep(motor.rotate(180, 32))
sleep(motor.rotate(-180, 16))
motor.stop()
```

The function `translate(blocks, [rpm])` will return the time it will take to rotate the shaft to push a piston or gantry shaft by distance given by the argument *blocks* at the current speed. If the optional argument *rpm* is given it will set the speed of the motor and return the action time at the new speed.
```lua
motor.setSpeed(32)
sleep(motor.translate(5))
motor.stop()
```
In the following example, the motor attached to a piston will extend the piston by 5 blocks, stop for a second, retract, and then finally stop.
```lua
local motor = peripheral.wrap("left")
sleep(motor.translate(5, 32))
sleep(1)
sleep(motor.translate(-5, 32))
motor.stop()
```

The function `getSpeed()` will return the current motor speed.
```lua
local rpm = motor.getSpeed()
```
The function `getStressCapacity()` will return the produced stress capacity (output su).
```lua
local su = motor.getStressCapacity()
```
The function `getEnergyConsumption()` will return the motor energy consumption in FE/t.
```lua
local fe = motor.getEnergyConsumption()
```
The function `getMaxInsert()` will return the Motor max input in fe.
```lua
local fe = motor.getMaxInsert()
```
The function `getMaxExtract()` will return the Motor max output in fe (Always 0).
```lua
local fe = motor.getMaxExtract()
```
The function `getType()` will return the motor peripheral name, which will always be "electric_motor".
```lua
print("Peripheral: " .. motor.getType())
```
# Servo Motor
In the following example, we get the peripheral of a Servo Motor below the computer.
```lua
local servo = peripheral.wrap("bottom")
```
The Servo Motor turns the blocks in front of it to a target angle. Redstone on its top side turns it towards its Max Angle and redstone on the opposite side towards its Min Angle. The two signals are summed, with the Min side counting as negative, and a summed signal of 15 turns the servo all the way to the limit. Without a signal the target is 0 degrees. The setters below change the speed and the limits, and the servo only moves while a signal is on. It also needs energy (fe) to move.

The function `setSpeed(rpm)` sets the speed at which the servo turns, a number between `-256` and `256` (the `motor_rpm_range` in the `[electric_motor]` section of the common config). It throws an exception for a speed out of that range or a value that is not a finite number.
```lua
servo.setSpeed(64)
```
The function `getSpeed()` will return the set speed in RPM.
```lua
local rpm = servo.getSpeed()
```
The function `getAngle()` will return the current angle in degrees, and `getTargetAngle()` the angle the servo turns to.
```lua
local angle = servo.getAngle()
local target = servo.getTargetAngle()
```
The function `setMaxAngle(degrees)` sets the Max Angle, a number between `0` and `180`. The function `getMaxAngle()` will return it.
```lua
servo.setMaxAngle(120)
local max = servo.getMaxAngle()
```
The function `setMinAngle(degrees)` sets the Min Angle, a number between `0` and `180` that counts towards the other side. The function `getMinAngle()` will return it as a negative number, `-90` by default.
```lua
servo.setMinAngle(45)
local min = servo.getMinAngle()
```
The function `setTargetAngle(angle)` takes an angle between `-180` and `180`. A positive angle becomes the Max Angle and sets the Min Angle to `0`. A negative angle becomes the Min Angle and sets the Max Angle to `0`. The servo then turns to that angle while a signal of 15 is on the matching side. All the setters throw an exception for a value out of range or not a finite number.
```lua
servo.setTargetAngle(45)
```
In the following example, the computer sits on the top side of the servo and gives it the signal itself. The servo assembles when the signal comes on, turns to 45 degrees, then to 90 degrees.
```lua
local servo = peripheral.wrap("bottom")
servo.setTargetAngle(45)
redstone.setAnalogOutput("bottom", 15)
sleep(2)
servo.setTargetAngle(90)
```
The function `isAssembled()` will return true while the servo moves a contraption. The function `assemble()` assembles the blocks in front of the servo, and `disassemble()` places them back in the world.
```lua
if not servo.isAssembled() then
  servo.assemble()
end
```
The function `getEnergyConsumption()` will return the servo energy consumption in fe/t at the set speed. It draws that while it turns, a tenth of it while it holds its angle and nothing while it is not assembled.
```lua
local fe = servo.getEnergyConsumption()
```
The Servo Motor has no `getType()` function of its own. The function `peripheral.getType` will return "servo_motor".
```lua
print("Peripheral: " .. peripheral.getType(servo))
```
# Accumulator
In the following example, we get the peripheral of an Accumulator on the left.
```lua
local accumulator = peripheral.wrap("left")
```
The function `getEnergy()` will return the accumulator total stored charge in fe.
```lua
local fe = accumulator.getEnergy()
```
The function `getCapacity()` will return the accumulator total capacity in fe.
```lua
local fe = accumulator.getCapacity()
```
The function `getPercent()` will return the accumulator total charge in relation to the total capacity in percent.
```lua
local percent = accumulator.getPercent()
```
The function `getMaxInsert()` will return the accumulator max input per block face in fe.
```lua
local fe = accumulator.getMaxInsert()
```
The function `getMaxExtract()` will return the accumulator max output per block face in fe.
```lua
local fe = accumulator.getMaxExtract()
```
The function `getHeight()` will return the accumulator height in block.
```lua
local blocks = accumulator.getHeight()
```
The function `getWidth()` will return the accumulator width in block.
```lua
local blocks = accumulator.getWidth()
```
The function `getType()` will return the accumulator peripheral name, which will always be "modular_accumulator".
```lua
print("Peripheral: " .. accumulator.getType())
```
# Portable Energy Interface (PEI)
In the following example, we get the peripheral of a PEI on the left.
```lua
local pei = peripheral.wrap("left")
```
The function `getEnergy()` will return the connected contraption total stored charge in fe, (-1 if not connected).
```lua
local fe = pei.getEnergy()
```
The function `getCapacity()` will return the connected contraption total capacity in fe, (-1 if not connected).
```lua
local fe = pei.getCapacity()
```
The function `isConnected()` will return true if a contraption is connected.
```lua
local connected = pei.isConnected()
```
The function `getMaxInsert()` will return the PEI max input in fe.
```lua
local fe = accumulator.getMaxInsert()
```
The function `getMaxExtract()` will return the PEI max output in fe.
```lua
local fe = accumulator.getMaxExtract()
```
The function `getType()` will return the PEI peripheral name, which will always be "portable_energy_interface".
```lua
print("Peripheral: " .. pei.getType())
```
# Redstone Relay
In the following example, we get the peripheral of a Redstone Relay on the left.
```lua
local relay = peripheral.wrap("left")
```
The function `getMaxInsert()` will return the Relay max input in fe.
```lua
local fe = relay.getMaxInsert()
```
The function `getMaxExtract()` will return the Relay max output in fe.
```lua
local fe = relay.getMaxExtract()
```
The function `getThroughput()` will return the current throughput in fe.
```lua
local fe = relay.getThroughput()
```
The function `isPowered()` will return the redstone state of the Relay.
```lua
local powered = relay.isPowered()
```
The function `getType()` will return the Relay peripheral name, which will always be "redstone_relay".
```lua
print("Peripheral: " .. relay.getType())
```
# Digital Adapter
In the following example, we get the peripheral of a Digital Adapter on the left.
```lua
local da = peripheral.wrap("left")
```
The function `getType()` will return the Adapter peripheral name, which will always be "digital_adapter".
```lua
print("Peripheral: " .. da.getType())
```
### Rotational Speed Controller
The function `setTargetSpeed(side, speed)` will set the target speed of a Rotation Speed Controller attached to the side of a Digital Adapter.
```lua
da.setTargetSpeed("top", 64)
```
The function `getTargetSpeed(side, speed)` will get the target speed of a Rotation Speed Controller attached to the side of a Digital Adapter.
```lua
local speed = da.getTargetSpeed("top")
```
### Stressometer
The function `getKineticStress(side)` will get the stress of a Stressometer attached to the side of a Digital Adapter.
```lua
local stress = da.getKineticStress("bottom")
```
The function `getKineticCapacity(side)` will get the stress capacity of a Stressometer attached to the side of a Digital Adapter.
```lua
local capacity = da.getKineticCapacity("bottom")
```
### Speedometer
The function `getKineticSpeed(side)` will get the speed of a Speedometer attached to the side of a Digital Adapter.
```lua
local speed = da.getKineticSpeed("north")
```
The function `getKineticTopSpeed()` will get the top speed as set by Create.
```lua
local topSpeed = da.getKineticTopSpeed()
```
### Pulleys
The function `getPulleyDistance(side)` will get the extended distance of a Rope, Hose, or Elevator -Pulley attached to the side of a Digital Adapter.
```lua
local blocks = da.getPulleyDistance("south")
```
### Elevator Pulley
The function `getElevatorFloor(side)` will get the current floor index of an Elevator Pulley attached to the side of a Digital Adapter.
```lua
local floor = da.getElevatorFloor("south")
```
The function `getElevatorFloors(side)` will get the number of floors of an Elevator Pulley attached to the side of a Digital Adapter.
```lua
local floorCount = da.getElevatorFloors("south")
```
The function `getElevatorFloorName(side, index)` will get floor name at floor index of a Elevator Pulley attached to the side of a Digital Adapter.
```lua
local floorName = da.getElevatorFloorName("south", 0)
```
The function `gotoElevatorFloor(side, index)` will trigger a Elevator Pulley attached to the side of a Digital Adapter to move to the given floor index and returns the delta-y to move.
```lua
local floorName = da.gotoElevatorFloor("south", 0)
```
The function `hasElevatorArrived(side)` will return true if a Elevator Pulley attached to the side of a Digital Adapter has arrived at a floor.
```lua
local arrived = da.hasElevatorArrived("south")
```
### Mechanical Piston
The function `getPistonDistance(side)` will get the extended distance of a Mechanical Piston attached to the side of a Digital Adapter.
```lua
local blocks = da.getPistonDistance("east")
```
### Mechanical Bearing
The function `getBearingAngle(side)` will get the angle of a Mechanical Bearing attached to the side of a Digital Adapter.
```lua
local degrees = da.getBearingAngle("west")
```
### Display Link
The function `print(text)` will print a string on the currently selected line to an internal buffer which can be read by a Display Link and put on a Display Board, print will increment the currently selected line.
```lua
print("Hello World!")
```
The function `clearLine()` will clear the text on the currently selected line.

The function `clear()` will clear all the text on all lines.

The function `getLine()` will return the currently selected line (starts at 1).

The function `setLine(line)` will set the currently selected line to *line* (starts at 1).
```lua
da.print("Text on first line")
da.print("Text on second line")
da.setLine(1)
da.print("Text on first line again")
```
The function `getMaxLines()` will return the max number of lines that can be displayable using the Digital Adapter (will always return 16).

### Other

The function `getDurationDistance(blocks, rpm)` will return the time needed to push a Mechanical Piston, Pulley or Gantry a number of blocks at the given rpm.

The function `getDurationAngle(degrees, rpm)` will return the time needed to rotate a Mechanical Bearing by a number of degrees at the given rpm.
