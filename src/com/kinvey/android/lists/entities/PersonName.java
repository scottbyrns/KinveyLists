package com.kinvey.android.lists.entities;

import java.util.Arrays;
import java.util.List;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class PersonName implements MappedEntity {

	private String firstName;
	private String lastName;
	private String id;
	
	public PersonName () {}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] {
				new MappedField("id", "_id"),
				new MappedField("firstName", "fn"),
				new MappedField("lastName", "ln")
		});
	}
	
	public String toString() {
		return firstName + " " + lastName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
