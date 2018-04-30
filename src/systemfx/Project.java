/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemfx;

/**
 *
 * @author user
 */
class Project {
    private String id;
    private String ProjectName;
    
    public Project(String name, String id) {
        this.id = id;
        this.ProjectName = name;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the ProjectName
     */
    public String getProjectName() {
        return ProjectName;
    }

    /**
     * @param ProjectName the ProjectName to set
     */
    public void setProjectName(String ProjectName) {
        this.ProjectName = ProjectName;
    }
    public String toString(){
        return this.ProjectName + "-" + this.id;
    }
}
