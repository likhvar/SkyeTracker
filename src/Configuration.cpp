#include "Configuration.h"

#define DS1307_RAM_SIZE 56

namespace SkyeTracker
{

	Configuration::Configuration(RTC_DS1307* rtc)
	{
		_rtc = rtc;
	}


	Configuration::~Configuration()
	{
	}

	void Configuration::setDual(bool val)
	{
		_dualAxis = val;
	}

	void Configuration::SetLocation(float lat, float lon)
	{
		_latitude = lat;
		_longitude = lon;
	}

	void Configuration::SetLimits(float minAzimuth, float maxAzimuth, float minElevation, float maxElevation)
	{
		_eastAzimuth = minAzimuth;
		_westAzimuth = maxAzimuth;
		_minimumElevation = minElevation;
		_maximumElevation = maxElevation;
		// verify limits
		if (_maximumElevation > 90)
		{
			_maximumElevation = 90;
		}
		if (_maximumElevation < 45)
		{
			_maximumElevation = 45;
		}

		if (_minimumElevation > 45)
		{
			_minimumElevation = 45;
		}
		if (_minimumElevation < 0)
		{
			_minimumElevation = 0;
		}

		if (_eastAzimuth < 45)
		{
			_eastAzimuth = 45;
		}
		if (_eastAzimuth > 135)
		{
			_eastAzimuth = 135;
		}

		if (_westAzimuth < 225)
		{
			_westAzimuth = 225;
		}
		if (_westAzimuth > 315)
		{
			_westAzimuth = 315;
		}
	}

	void Configuration::SetUTCOffset(int val)
	{
		_timeZoneOffsetToUTC = val;
	}

	float deserialFloat(byte* buffer) {
		float f = 0;
		byte * b = (byte *)&f;
		b[0] = buffer[0];
		b[1] = buffer[1];
		b[2] = buffer[2];
		b[3] = buffer[3];
		return f;
	}

	void Configuration::Load()
	{
		byte _buffer[DS1307_RAM_SIZE+1];
		_rtc->readnvram(_buffer, DS1307_RAM_SIZE, 0);
		_dualAxis = _buffer[0];
		_timeZoneOffsetToUTC = _buffer[1];
		_eastAzimuth = deserialFloat(&(_buffer[2]));
		_westAzimuth = deserialFloat(&(_buffer[6]));
		_minimumElevation = deserialFloat(&(_buffer[10]));
		_maximumElevation = deserialFloat(&(_buffer[14]));
		_latitude = deserialFloat(&(_buffer[18]));
		_longitude = deserialFloat(&(_buffer[22]));
	}

	void serialFloat(byte * buffer, float f) {
		byte * b = (byte *)&f;
		buffer[0] = b[0];
		buffer[1] = b[1];
		buffer[2] = b[2];
		buffer[3] = b[3];
	}


	void Configuration::Save()
	{
		byte _buffer[DS1307_RAM_SIZE+1];
		for (int i = 0; i < DS1307_RAM_SIZE; i++)
		{
			_buffer[i] = NULL;
		}
		if (_dualAxis)
			_buffer[0] = 1;
		else
			_buffer[0] = 0;
		_buffer[1] = (unsigned char)_timeZoneOffsetToUTC;
		serialFloat(&(_buffer[2]), _eastAzimuth);
		serialFloat(&(_buffer[6]), _westAzimuth);
		serialFloat(&(_buffer[10]), _minimumElevation);
		serialFloat(&(_buffer[14]), _maximumElevation);
		serialFloat(&(_buffer[18]), _latitude);
		serialFloat(&(_buffer[22]), _longitude);
		_rtc->writenvram(0, _buffer, DS1307_RAM_SIZE);

		for (int i = 0; i < DS1307_RAM_SIZE; i++)
		{
			Serial.print(_buffer[i], HEX);
			Serial.print(" ");
		}
	}


	void Configuration::PrintJson()
	{
		ConfigTransfer ct;
		ct._dual = isDual();
		ct._eastAz = getEastAzimuth();
		ct._westAz = getWestAzimuth();
		ct._maxElevation = getMaximumElevation();
		ct._minElevation = getMinimumElevation();
		ct._lat = getLat();
		ct._long = getLon();
		ct._offsetToUTC = getTimeZoneOffsetToUTC();
		ct.PrintJson();
	}
}