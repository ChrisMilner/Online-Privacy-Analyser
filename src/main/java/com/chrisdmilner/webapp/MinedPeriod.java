package com.chrisdmilner.webapp;

/*
 * Mined Period
 *
 * Stores information about a particular period of time in the user's life, either a period of education or work.
 *
 * */
public class MinedPeriod {

    private String discipline;      // The discipline of study or the job title.
    private String institution;     // The school or place of work.
    private String startYear;       // The year they started there.
    private String endYear;         // The year they left there.

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

    // Converts the period to a human readable version for command line output.
    public String toString() {
        return discipline + " at " + institution + " (" + startYear + " - " + endYear + ")";
    }

}
