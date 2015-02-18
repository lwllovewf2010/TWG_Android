package com.modusgo.ubi.jastec;


public class DriveRecodePerTrip{
	public byte STX;
    public DriveTrait Drive_Trait = new DriveTrait();
    public long CountOfRecord;
    public short FaultType;    // 첫번째 시스템의 결함타입

    public Byte[] Fault = new Byte[10];        //첫번째 시스템의 결함 내용

    public short DrvGroupSptd;
     
    public Byte[] Reserved = new Byte[10];
    public byte ETX;
    
    
    	

	public static DriveRecodePerTrip createDriveRecodePerTrip()
    {
    	return new DriveRecodePerTrip();
    }

	public static Accelation createAccelation()
    {
    	return new Accelation();
    }
    // group 0
    public static class Accelation
    {
    	public static final int SIZE = 4;
    	
        public short Increment;
        public short Decrement;
        
        public void parse(Byte[] data)
        {
        	Increment = Utils.toShort(data, 0);
    		Decrement = Utils.toShort(data, 2);
        }
		
    } // 4byte

    public static AverageStruct createAverageStruct()
    {
    	return new AverageStruct();
    }
    
    public static class AverageStruct
    {
    	public static final int SIZE = 12;
        
        public long AverageSpeed;
        public long TotalVal;
        public long Cnt;
        
        public void parse(Byte[] data)
        {
        	AverageSpeed = Utils.toLong4Byte(data, 0);
    		TotalVal = Utils.toLong4Byte(data, 4);
    		Cnt = Utils.toLong4Byte(data, 8);
        }
		
		
    } // 12byte
    
    public static DriveTime createDriveTime()
    {
    	return new DriveTime();
    }

    public static class DriveTime
    {
    	public static final int SIZE = 32;
        
        public long IdleTime;         // msec
        public long EngRunTime;       // msec
        public long CommonTime;       // msec
        public long FuelCutTime;      // msec
        public long EcoTime;          // msec
        public long AccelTime;        // msec
        public long OverSpeedTime;    // msec
        public long WarmUpTime;       // msec 시동후 차가 출발하기 전까지의 시간
        
        public void parse(Byte[] data)
        {
        	IdleTime = Utils.toLong4Byte(data, 0);
    		EngRunTime = Utils.toLong4Byte(data, 4);
    		CommonTime = Utils.toLong4Byte(data, 8);
    		FuelCutTime = Utils.toLong4Byte(data, 12);
    		EcoTime = Utils.toLong4Byte(data, 16);
    		AccelTime = Utils.toLong4Byte(data, 20);
    		OverSpeedTime = Utils.toLong4Byte(data, 24);
    		WarmUpTime = Utils.toLong4Byte(data, 28);	
        }
        
       
    } // 32 byte

    public static SpeedDivid createSpeedDivid()
    {
    	return new SpeedDivid();
    }
    
    public static class SpeedDivid
    {
    	public static final int SIZE = 64;
        
        public long LessThan10Km;
        public long LessThan20Km;
        public long LessThan30Km;
        public long LessThan40Km;
        public long LessThan50Km;
        public long LessThan60Km;
        public long LessThan70Km;
        public long LessThan80Km;
        public long LessThan90Km;
        public long LessThan100Km;
        public long LessThan110Km;
        public long LessThan120Km;
        public long LessThan130Km;
        public long LessThan140Km;
        public long LessThan150Km;
        public long OverThan150Km;
        
        public void parse(Byte[] data)
        {
        	LessThan10Km = Utils.toLong4Byte(data, 0);
    		LessThan20Km = Utils.toLong4Byte(data, 4);
    		LessThan30Km = Utils.toLong4Byte(data, 8);
    		LessThan40Km = Utils.toLong4Byte(data, 12);
    		LessThan50Km = Utils.toLong4Byte(data, 16);
    		LessThan60Km = Utils.toLong4Byte(data, 20);
    		LessThan70Km = Utils.toLong4Byte(data, 24);
    		LessThan80Km = Utils.toLong4Byte(data, 28);
    		LessThan90Km = Utils.toLong4Byte(data, 32);
    		LessThan100Km = Utils.toLong4Byte(data, 36);
    		LessThan110Km = Utils.toLong4Byte(data, 40);
    		LessThan120Km = Utils.toLong4Byte(data, 44);
    		LessThan130Km = Utils.toLong4Byte(data, 48);
    		LessThan140Km = Utils.toLong4Byte(data, 52);
    		LessThan150Km = Utils.toLong4Byte(data, 56);
    		OverThan150Km = Utils.toLong4Byte(data, 60);
        }
		
        
    } // 64 byte
    
    public static AverageSpeed createAverageSpeed()
    {
    	return new AverageSpeed();
    }

    public static class AverageSpeed
    {
    	public static final int SIZE = 12;
    	
        public long Average_Speed; // TotDistance / TotTime
        public long TotDistance;  // meter, 소수점 4번째에서 반올림..
        public long TotTime;      // msec
		
        public void parse(Byte[] data)
        {
        	Average_Speed = Utils.toLong4Byte(data, 0);
    		TotDistance = Utils.toLong4Byte(data, 4);
    		TotTime = Utils.toLong4Byte(data, 8);
        }
        
    } // 12 bytes
    
    public static FuelConsum createFuelComsum()
    {
    	return new FuelConsum();
    }

    public static class FuelConsum
    {
    	public static final int SIZE = 16;
    	
        public long FuelConsumMass;       // 연료 소비량
        public float FuelEfficient;         // 연비 TotDisDistance / FuelConsumMass
        public float FuelConsumRate;        // 연료 소비율
        public float FuelConsumEfficient;   // 연료 소비효율  // TotDistance / FuelConsumRate
        
        public void parse(Byte[] data)
        {
        	FuelConsumMass = Utils.toLong4Byte(data, 0);
    		FuelEfficient = Utils.toFloat(data, 4);
    		FuelConsumRate = Utils.toFloat(data, 8);
    		FuelConsumEfficient = Utils.toFloat(data, 12);
        }
        
        
    } // 16 bytes


    public static Co2Struct createCo2Struct()
    {
    	return new Co2Struct();
    }
    
    public static class Co2Struct
    {
    	public static final int SIZE = 12;
    	
        public long CoPerKm;  // average value unit g/km
        public long CoMass;   // CO2 gram
        public long Distance; // meter
        
        public void parse(Byte[] data)
        {
        	CoPerKm = Utils.toLong4Byte(data, 0);
    		CoMass = Utils.toLong4Byte(data, 4);
    		Distance = Utils.toLong4Byte(data, 8);
        }
		
        
    } // 12 bytes
    
    public static DriveTrait createDriveTrait()
    {
    	return new DriveTrait();
    }

    public static class DriveTrait
    {
    	public static final int SIZE = 172; 
    		
        public Calenda_RTC StartTime = new Calenda_RTC();
        public Calenda_RTC StopTime = new Calenda_RTC();
        public Accelation Accel = new Accelation();             // group 1 count
        public AverageStruct Battery = new AverageStruct();     // group 2 Voltage 엔진 rpm 발생시 체크 
        public float MaxCoolTemp;                               // group 3 'C
        public short MaxSpeed;                                 // group 4 Km/h
        public AverageSpeed AverageSpeed = new AverageSpeed();                       // group 5
        public SpeedDivid SpeedDivid = new SpeedDivid();                            // group 6 msec
        public DriveTime DrivingTime = new DriveTime();                           // group 7
        public FuelConsum Fuel = new FuelConsum();                                 // group 8
        public Co2Struct CO2 = new Co2Struct();                                   // group 9 CO2
        
		
    }// 7 + 7 + 4 + 12 + 4 + 2 + 12 + 64 + 32 + 16 + 12
    
    
}
