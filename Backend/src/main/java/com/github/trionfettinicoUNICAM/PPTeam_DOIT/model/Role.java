package com.github.trionfettinicoUNICAM.PPTeam_DOIT.model;

import java.util.Objects;

/**
 * Describes the role a {@link User} has inside a {@link Team}. It is responsible for associating a {@link User}
 * to the {@link Skill} for which it is needed in the {@link Project}.
 */
public class Role{

    // TODO: 10/12/20 scrivere il javadoc di questi metodi (lasciati indietro perche comunque si spiegano gia bene da soli)

    private Skill skill;
    private String userMail;
    private String projectName;

    public Role(Skill skill, String userMail, String projectName) throws IllegalArgumentException {
        setSkill(skill);
        setUserMail(userMail);
        setProjectName(projectName);
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill= Objects.requireNonNull(skill, "Skill is Null");;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) throws IllegalArgumentException {
        if(userMail.length() == 0) throw new IllegalArgumentException("UserMail is empty");
        this.userMail=userMail;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) throws IllegalArgumentException {
        if(projectName.length() == 0) throw new IllegalArgumentException("ProjectName is empty");
        this.projectName=projectName;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Role realRole = (Role) object;
        return userMail.equals(realRole.userMail) && projectName.equals(realRole.projectName);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), userMail, projectName);
    }
}
