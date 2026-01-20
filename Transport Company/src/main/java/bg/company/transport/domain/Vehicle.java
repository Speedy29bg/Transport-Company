package bg.company.transport.domain;

import java.util.Objects;

public class Vehicle {
    private Long id;
    private Long companyId;
    private String regNumber;
    private VehicleType type;
    private Integer seatCount;
    private Integer maxLoadKg;
    private Integer volumeLiters;

    public Vehicle() {
    }

    public Vehicle(Long id, Long companyId, String regNumber, VehicleType type, Integer seatCount, Integer maxLoadKg, Integer volumeLiters) {
        this.id = id;
        this.companyId = companyId;
        this.regNumber = regNumber;
        this.type = type;
        this.seatCount = seatCount;
        this.maxLoadKg = maxLoadKg;
        this.volumeLiters = volumeLiters;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public Integer getMaxLoadKg() {
        return maxLoadKg;
    }

    public void setMaxLoadKg(Integer maxLoadKg) {
        this.maxLoadKg = maxLoadKg;
    }

    public Integer getVolumeLiters() {
        return volumeLiters;
    }

    public void setVolumeLiters(Integer volumeLiters) {
        this.volumeLiters = volumeLiters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", companyId=" + companyId +
                ", regNumber='" + regNumber + '\'' +
                ", type=" + type +
                ", seatCount=" + seatCount +
                ", maxLoadKg=" + maxLoadKg +
                ", volumeLiters=" + volumeLiters +
                '}';
    }
}
