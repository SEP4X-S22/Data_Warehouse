package com.Apharma.sep4.Model;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Sensors")
public class Sensor
{
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private SensorType sensorType;
  private double constraintMinValue;
  private double constraintMaxValue;
//  @OneToMany(targetEntity = Reading.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//  @JoinColumn(name= "sensor_reading_fk", referencedColumnName = "id")
//  private List<Reading> readings = new ArrayList<>();
  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Reading> readings;
  @ManyToOne
  @JoinColumn(name = "room_id")
  private Room room;

  public Sensor(){
    readings = new ArrayList<>();
  }

  public List<Reading> getReadings()
  {
    return readings;
  }

  public void setReadings(List<Reading> readings)
  {
    this.readings = readings;
  }

  public Room getRoom()
  {
    return room;
  }

  public void setRoom(Room room)
  {
    this.room = room;
    room.getSensors().add(this);
  }
  
  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public SensorType getSensorType()
  {
    return sensorType;
  }

  public void setSensorType(SensorType sensorType)
  {
    this.sensorType = sensorType;
  }

  public double getConstraintMinValue()
  {
    return constraintMinValue;
  }

  public void setConstraintMinValue(double constraintMinValue)
  {
    this.constraintMinValue = constraintMinValue;
  }

  public double getConstraintMaxValue()
  {
    return constraintMaxValue;
  }

  public void setConstraintMaxValue(double constraintMaxValue)
  {
    this.constraintMaxValue = constraintMaxValue;
  }

  @Override public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Sensor sensor1 = (Sensor) o;
    return id == sensor1.id && sensorType == sensor1.sensorType;
  }

  @Override public int hashCode()
  {
    return Objects.hash(id, sensorType);
  }
  
  @Override public String toString()
  {
    return "Sensor{" + "id=" + id + ", sensor=" + sensorType + ", readings="
                   +'}';
  }
  
  public enum SensorType
  {
    Humidity, CO2, Light, Temperature
  }
}