import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.FileNotFoundException;
import java.io.File;

/**
 * @author Daniel Johnson, Jonathon Elfar
 * Provides a text user interface for interacting with a HashTable of Students.
 */
public class HTDriver {
    /**
     * Handles all input and output. First it loads from a file, then lets
     * the user perform operations on the resulting HashTable.
     */
    public static void main(String[] args) {

        //make our scanner and table
        Scanner sc = new Scanner(System.in);
        HashTable table = null;

        //repeatedly try to load the input file until it's successful
        while(table == null) {
            System.out.println("What is the name of the input file? ");
            try {
                table = loadFile(new File(sc.nextLine()));
            } catch (LoadFileException e) {
                System.out.println(e.getMessage());
            }
        }

        //Present menu
        System.out.println();
        System.out.println("Choose one of the following operations:");
        System.out.println("a - add the element");
        System.out.println("d - delete the element");
        System.out.println("f - find and retrieve the element");
        System.out.println("n - get the number of elements in the collection");
        System.out.println("e - check if the collection is empty");
        System.out.println("k - make the hash table empty");
        System.out.println("p - print the content of the hash table");
        System.out.println("o - output the elements of the collection");
        System.out.println("q - Quit");

        String choice;
        //keep prompting for input until they enter "q"
        while(!(choice = sc.next().substring(0, 1).toLowerCase()).equals("q")) {
            sc.nextLine(); //clear input buffer
            System.out.println(); //print a newline to format it nicely

            //try to perform the specified action. any invalid input will
            //throw an exception, which we will catch and print at the end
            try {
                switch (choice.charAt(0)) {
                    case 'a':
                        //insert student
                        System.out.print("Enter a student record: ");
                        if(!insertStudent(table,sc.nextLine()))
                            System.out.println("Student alreay exists.");
                        else
                            System.out.println("Student successfully inserted.");
                        break;
                    case 'd':
                        //delete student
                        System.out.print("Enter an id: ");
                        if(!table.delete(makeSearchStudent(sc.nextLine()))) 
                            System.out.println("Student not found.");
                        else
                            System.out.println("Student deleted.");
                        break;
                    case 'f':
                        //find student
                        System.out.print("Enter an id: ");
                        Student foundStudent = (Student)table.find(
                         makeSearchStudent(sc.nextLine()));
                        //table.find returns null if no student was found
                        if(foundStudent != null) {
                            System.out.println("Found record with inputted key: "
                             + foundStudent);
                        } else {
                            System.out.println("No student found.");
                        }
                        break;
                    case 'n':
                        System.out.println("Number of elements: "
                         + table.elementCount());
                        break;
                    case 'e':
                        if(table.isEmpty()) {
                            System.out.println("Table is empty.");
                        } else {
                            System.out.println("Table is not empty.");
                        }
                        break;
                    case 'k':
                        table.makeEmpty();
                        System.out.println("Table is now empty.");
                        break;
                    case 'p':
                        table.printTable();
                        break;
                    case 'o':
                        table.outputData();
                        break;
                    default:
                        System.out.println("Please enter a valid operation.");
                        break;
                }
            } catch(InvalidStudentException e) {
                //if any of the input was invalid, display the specified message.
                System.out.println(e.getMessage());
            }
            System.out.println();
            System.out.print("Choose an operation: ");
        }
        System.out.println("Thank you for being such a great user ;)");
    }

    //Loads student records from a file and returns a HashTable containing them
    //It will throw a LoadFileException if anything goes wrong.
    private static HashTable loadFile(File file) throws LoadFileException {
        try {
            //make a scanner for the file
            Scanner fileScanner = new Scanner(file);
            //get the number of students to import
            int collectionSize = fileScanner.nextInt();
            fileScanner.nextLine(); //clear the buffer
            //create a new table with the specified size
            HashTable table = new HashTable(collectionSize);
            //read student objects from file. loop until we hit the end of the
            //file, or until we've read the specified number of records
            for(int i=0;i<collectionSize&&fileScanner.hasNextLine();i++) {
                //insert each student
                try {
                    insertStudent(table,fileScanner.nextLine());
                } catch(InvalidStudentException e) {} //ignore invalid students
            }
            return table; //finally return the table
        }
        catch (FileNotFoundException e) {
            //if something goes wrong, throw an exception with what went wrong
            throw new LoadFileException("File not found.");
        } catch (NoSuchElementException e) {
            throw new LoadFileException(
             "First line must contain collection size.");
        }
    }
    //exception for the above method
    private static class LoadFileException extends Exception {
        public LoadFileException(String message) {super(message);}
    }
    //parses student record string "line" and inserts a Student into "table"
    private static boolean insertStudent(HashTable table, String line)
     throws InvalidStudentException {
        try {
            //scan the string they passed us
            Scanner lineScanner = new Scanner(line);
            //get the id and name
            long id = lineScanner.nextLong();
            String name = lineScanner.next();
            //if the id is valid (larger than 0) and the we've read the full
            //string, then insert the student and return
            if(!lineScanner.hasNext() && id > 0) {
                return table.insert(new Student(id, name));
            }
            //otherwise, we'll not return and throw an exception
        } catch(NoSuchElementException e) {} //stop if the string is invalid
        //if we haven't returned by now, something went wrong.
        throw new InvalidStudentException("Invalid Student");
    }
    //same as insertStudent, but it returns a blank student with the
    //specified ID for searching the table
    private static Student makeSearchStudent(String line)
     throws InvalidStudentException {
        try {
            Scanner lineScanner = new Scanner(line);
            long id = lineScanner.nextLong();
            if(!lineScanner.hasNext() && id > 0) {
                return new Student(id, null);
                //return a student with the id they're searching for, but
                //leave name blank. this student is "equal" to the student
                //we're searching for, so it can be used in the find and
                //delete methods of HashTable even if we don't know the
                //student's name.
            }
        } catch(NoSuchElementException e) {}
        throw new InvalidStudentException("Invalid Student");
    }
    //exception for the above methods
    private static class InvalidStudentException extends Exception {
        public InvalidStudentException(String message) {super(message);}
    }
}
