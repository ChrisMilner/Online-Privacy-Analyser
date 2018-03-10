package com.chrisdmilner.webapp;

public class MinedPeriod {

    private String discipline;
    private String institution;
    private String startYear;
    private String endYear;

    public MinedPeriod(String discipline, String institution, String startYear, String endYear) {
        this.discipline = discipline;
        this.institution = institution;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public String getDiscipline() {
        return discipline;
    }

    public String getInstitution() {
        return institution;
    }

    public String getStartYear() {
        return startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public String toString() {
        return discipline + " at " + institution + " (" + startYear + " - " + endYear + ")";
    }

}
