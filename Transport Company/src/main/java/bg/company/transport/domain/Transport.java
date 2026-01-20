package bg.company.transport.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Transport {
    private Long id;
    private Long companyId;
    private Long clientId;
    private String origin;
    private String destination;
    private OffsetDateTime departAt;
    private OffsetDateTime arriveAt;
    private CargoKind cargoKind;
    private Integer cargoWeightKg;
    private Integer passengerCount;
    private BigDecimal price;
    private Long driverId;
    private Long vehicleId;
    private PaymentStatus paymentStatus;
    private OffsetDateTime createdAt;

    public Transport() {
    }

    public Transport(Long id, Long companyId, Long clientId, String origin, String destination, OffsetDateTime departAt,
                     OffsetDateTime arriveAt, CargoKind cargoKind, Integer cargoWeightKg, Integer passengerCount,
                     BigDecimal price, Long driverId, Long vehicleId, PaymentStatus paymentStatus, OffsetDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.clientId = clientId;
        this.origin = origin;
        this.destination = destination;
        this.departAt = departAt;
        this.arriveAt = arriveAt;
        this.cargoKind = cargoKind;
        this.cargoWeightKg = cargoWeightKg;
        this.passengerCount = passengerCount;
        this.price = price;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public OffsetDateTime getDepartAt() {
        return departAt;
    }

    public void setDepartAt(OffsetDateTime departAt) {
        this.departAt = departAt;
    }

    public OffsetDateTime getArriveAt() {
        return arriveAt;
    }

    public void setArriveAt(OffsetDateTime arriveAt) {
        this.arriveAt = arriveAt;
    }

    public CargoKind getCargoKind() {
        return cargoKind;
    }

    public void setCargoKind(CargoKind cargoKind) {
        this.cargoKind = cargoKind;
    }

    public Integer getCargoWeightKg() {
        return cargoWeightKg;
    }

    public void setCargoWeightKg(Integer cargoWeightKg) {
        this.cargoWeightKg = cargoWeightKg;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transport)) return false;
        Transport transport = (Transport) o;
        return Objects.equals(id, transport.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Transport{" +
                "id=" + id +
                ", companyId=" + companyId +
                ", clientId=" + clientId +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", departAt=" + departAt +
                ", arriveAt=" + arriveAt +
                ", cargoKind=" + cargoKind +
                ", cargoWeightKg=" + cargoWeightKg +
                ", passengerCount=" + passengerCount +
                ", price=" + price +
                ", driverId=" + driverId +
                ", vehicleId=" + vehicleId +
                ", paymentStatus=" + paymentStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
