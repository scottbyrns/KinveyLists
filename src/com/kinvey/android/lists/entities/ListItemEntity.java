/**
 * 
 */
package com.kinvey.android.lists.entities;

import java.util.Arrays;
import java.util.List;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class ListItemEntity implements MappedEntity {

    private String id;
    private String name;
    private String desc;
    private String owner;
    private String due;

    public ListItemEntity() {
    };

    public ListItemEntity(String name, String desc, String owner, String due) {
        init(null, name, desc, owner, due);
    }

    public ListItemEntity(String id, String name, String desc, String owner, String due) {
        init(id, name, desc, owner, due);

    }

    private void init(String id, String name, String desc, String owner, String due) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.owner = owner;
        this.due = due;
    }

    /*
     * (non-Javadoc)
     * @see com.kcs.persistence.mapping.MappedClass#getPersistentFields()
     */
    @Override
	public List<MappedField> getMapping() {
        return Arrays
                .asList(new MappedField[] { new MappedField("id", "_id"), new MappedField("name", "item_name"), new MappedField("desc", "item_desc"),
                        new MappedField("owner", "owner"), new MappedField("due", "due") });
    }

    public String toString() {
        return name;
    }

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

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc
     *            the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the due
     */
    public String getDue() {
        return due;
    }

    /**
     * @param due
     *            the due to set
     */
    public void setDue(String due) {
        this.due = due;
    }

}
