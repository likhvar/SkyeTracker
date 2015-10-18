#pragma once

#if !defined(NOPOT)

#include <Arduino.h>
#include <ThreadController.h>
#include <Thread.h>
#include "Enumerations.h"
#include "LinearActuator.h"



#define noise 2
#define histeresis 2
#define shortCheckInterval 250
#define longCheckInterval 5000

namespace SkyeTracker
{

	class LinearActuatorWithPotentiometer : public Thread
	{
	public:
		LinearActuatorWithPotentiometer(int8_t positionSensor, int8_t enable, int8_t PWMa, int8_t PWMb);
		virtual ~LinearActuatorWithPotentiometer();

	private:
		float _requestedAngle;
		ActuatorState _state;
		int _extendedAngle; // angle when actuator is fully extended 
		int _retractedAngle; // angle when actuator is fully retracted
		int _enableActuator;
		int _PWMa;
		int _PWMb;
		float _lastPosition;
		float _extendedPosition; // reading from the actuators position sensor when fully extended
		float _retractedPosition; // reading from the actuators position sensor when fully retracted
		bool _foundExtendedPosition;
		int _positionSensor;
		float _position;

		bool IsMoving();
		float getCurrentPosition();
		
	protected:
		void run();
		float Range();

	public:
		void Initialize(int retractedAngle, int extendedAngle);
		ActuatorState getState() { return _state; };
		void MoveTo(float angle);
		float CurrentPosition();
		float CurrentAngle();
		void Retract();
		void MoveIn();
		void MoveOut();
		void Stop();
	};


}

#endif