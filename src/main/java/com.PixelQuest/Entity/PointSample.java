package com.PixelQuest.Entity;

import jakarta.persistence.*;

@Entity
public class PointSample {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private Point point;

  private long timestamp; // ms since start

  @ManyToOne
  @JoinColumn(name = "party_id")
  private Party party;

  public PointSample() {}

  public PointSample(Point point, long timestamp, Party party) {
    this.point = point;
    this.timestamp = timestamp;
    this.party = party;
  }

  public Point getPoint() {
    return point;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Party getParty() {
    return party;
  }

  public void setPoint(Point point) {
    this.point = point;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setParty(Party party) {
    this.party = party;
  }
}
