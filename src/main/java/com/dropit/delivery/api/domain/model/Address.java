package com.dropit.delivery.api.domain.model;

import java.util.Objects;

/**
 * Structured postal address.
 */
public final class Address {
	private final String street;
	private final String line1;
	private final String line2;
	private final String country;
	private final String postcode;
	private final String city;

	public Address(String street, String line1, String line2, String country, String postcode, String city) {
		this.street = street;
		this.line1 = line1;
		this.line2 = line2;
		this.country = country;
		this.postcode = postcode;
		this.city = city;
	}

	public String getStreet() { return street; }
	public String getLine1() { return line1; }
	public String getLine2() { return line2; }
	public String getCountry() { return country; }
	public String getPostcode() { return postcode; }
	public String getCity() { return city; }

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Address)) return false;
		Address address = (Address) o;
		return Objects.equals(street, address.street) && Objects.equals(line1, address.line1) && Objects.equals(line2, address.line2) && Objects.equals(country, address.country) && Objects.equals(postcode, address.postcode) && Objects.equals(city, address.city);
	}
	@Override public int hashCode() { return Objects.hash(street, line1, line2, country, postcode, city); }
}

