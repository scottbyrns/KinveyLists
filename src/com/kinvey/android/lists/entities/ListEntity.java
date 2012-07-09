package com.kinvey.android.lists.entities;

import java.util.Arrays;
import java.util.List;

import com.kinvey.KinveyMetadata;
import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

/**
 * @author shubhang
 */
public class ListEntity implements MappedEntity {
    private String id;
    private String name;
    private KinveyMetadata meta;


	/**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public KinveyMetadata getMeta() {
    	return meta;
    }
    
    public void setMeta(KinveyMetadata meta) {
    	this.meta = meta;
    }

    public ListEntity() {
    }

    public ListEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ListEntity(String name) {
        this.name = name;
    }

    
    /*
     * (non-Javadoc)
     * @see com.kcs.persistence.mapping.MappedClass#getPersistentFields()
     */
    @Override
    public List<MappedField> getMapping() {
        return Arrays.asList(new MappedField[] { new MappedField("id", "_id"),  
        										new MappedField("name", "name"), 
        										new MappedField("meta", KinveyMetadata.FIELD_NAME) });
    }

    public String toString() {
        return name;
    }

}
