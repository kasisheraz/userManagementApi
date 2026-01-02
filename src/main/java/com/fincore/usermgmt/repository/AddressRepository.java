package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Address entity operations.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find addresses by type code.
     * @param typeCode the address type code
     * @return list of addresses matching the type
     */
    List<Address> findByTypeCode(Integer typeCode);

    /**
     * Find addresses by country.
     * @param country the country name
     * @return list of addresses in the specified country
     */
    List<Address> findByCountry(String country);

    /**
     * Find addresses by city.
     * @param city the city name
     * @return list of addresses in the specified city
     */
    List<Address> findByCity(String city);

    /**
     * Find addresses by postal code.
     * @param postalCode the postal code
     * @return list of addresses with the specified postal code
     */
    List<Address> findByPostalCode(String postalCode);

    /**
     * Find addresses by status.
     * @param statusDescription the status description
     * @return list of addresses with the specified status
     */
    List<Address> findByStatusDescription(String statusDescription);

    /**
     * Find addresses created by a specific user.
     * @param createdBy the user ID who created the address
     * @return list of addresses created by the user
     */
    List<Address> findByCreatedBy(Long createdBy);
}
