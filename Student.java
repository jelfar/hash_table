/**
 * @author Daniel Johnson, Jonathon Elfar
 * Represents a single Student with an ID and a last name.
 * Students must have unique IDs, as they are considered
 * to be equal if their IDs are equal, and hash codes
 * are computed using only IDs.
 */
class Student {
    //student's id and last name
    private long id;
    private String lastName;

    /**
     * Creates a new student with the specified ID and last name.
     * @param id The student's ID number
     * @param lastName The student's last name
     */
    public Student(long id, String lastName) {
        this.id = id;
        this.lastName = lastName;
    }

    //override methods from Object.
    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other.getClass() != this.getClass()) return false;

        //two students are equal if their IDs are equal
        return this.id == ((Student)other).id;
    }

    @Override
    public String toString() {
        return id + ", " + lastName;
        //print out students in the format "ID, lastname"
    }

    @Override
    public int hashCode() {
        //give students hashcodes based on their ID.
        Long key = new Long(id);
        return key.hashCode(); //just use Long's hashCode method
    }
}