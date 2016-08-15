import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Daniel Johnson, Jonathon Elfar
 * A Hash Table implementation of a set of Objects.
 * Hashes Objects and stores them in an array with open addressing and
 * quadratic probing. Supports insert, find, and delete operations.
 */
public class HashTable {
    
    //an entry in the hash table
    private class HashEntry {
        //the element of the entry
        public Object element;
        //lazy deletion -- whether this element is actually in the set,
        //or if it used to be in the set but was deleted.
        public boolean active;
        //basic constructor, creates an active HashEntry with an element.
        public HashEntry(Object element) {
            this.element = element;
            this.active = true;
        }
    }

    //the array to store our HashEntries. For each cell: null means nothing
    //has been entered, an inactive HashEntry represents a deleted entry,
    //and an active HashEntry represents an element in the set.
    private HashEntry[] table;
    //the number of occupied cells (active OR inactive) so that we know
    //when we need to expand the table. Since we're using quadratic probing,
    //we want the table size to be the smallest prime larger than twice the
    //number of occupied cells.
    private int numOccupiedCells;

    /**
     * Creates an empty HashTable with a array size that's good for holding
     * element elements.
     * @param elements How many elements can be expected to be inserted.
     * Used to determine the optimal array size for storing the data.
     */
    public HashTable(int elements) {
        //create an empty HashTable, with a table sized by the smallest prime
        //number larger than or equal to twice the number of expected elements
        table = new HashEntry[findPrime(elements*2)];
        numOccupiedCells = 0; //the table is empty right now.
    }

    //finds the smallest prime larger than or equal to "start"
    private int findPrime(int start) {
        while(!isPrime(start)) {
            start++;
            //increment our value until it's prime
        }
        return start;
    }
    //adapted from
    //returns if n is prime or not
    private boolean isPrime(int n) {
        //handle n <= 2
        if (n<2) return false;

        for(int i = n-1; i > 1; i--) {
            if(n%i == 0)
                return false;
        }
        return true;
    }

    //iterator for iterating through the hash table
    private class Iter implements Iterator {
        //the index of the table that we're on now
        private int cursor;
        public Iter() {
            //initialize the cursor to the first active index in the table
            cursor = findNextActive(0);
        }
        public boolean hasNext() {
            //we have a next if the cursor is still within the table's
            //boundaries.
            return cursor < table.length;
        }
        public Object next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            //get the current value, move cursor to the next active element
            //that's at least 1 past the current element, and return value
            Object value = table[cursor].element;
            cursor = findNextActive(cursor + 1);
            return value;
        }
        //returns the next active cell greater than or equal to index
        private int findNextActive(int index) {
            //keep incrementing index until we get to the end of the table,
            //or until we hit an active cell (keep going while it's null
            //or inactive)
            while(index < table.length
             && (table[index] == null || !table[index].active)) {
                index++;
            }
            return index;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Inserts item into the set.
     * @param item The Object to add to the set
     */
    public boolean insert(Object item) {
        //don't try to do anything with a null item
        if(item == null) return false;

        //get the index that we can place item into
        int index = getIndexForKey(table, item, true);
        //set stopOnInactive to true, because we can overwrite
        //an inactive entry

        //if the entry at that index is null,
        if(table[index] == null) {
            //then create a new HashEntry for that spot
            table[index] = new HashEntry(item);
            //if the (now incremented) number of occupied cells
            //is too large for quadratic probing, rehash to the next
            //optimal size
            if(++numOccupiedCells >= (table.length/2)) {
                rehash(findPrime(table.length*2));
            }
        } else if(!table[index].active) {
            //if inactive, we can just update it to be active again
            table[index] = new HashEntry(item);
            //since occupiedcells didn't get incremented, we don't need to
            //check if rehashing is necessary.
        } else {
            return false;
        }
        return true;
    }

    //rehashes the entire table into a new table with size newSize
    private void rehash(int newSize) {
        //make a new array.
        HashEntry[] newTable = new HashEntry[newSize];
        int newTableIndex;

        //reset the number of occupied cells (since we won't rehash inactives)
        numOccupiedCells = 0;

        for(int i = 0; i < table.length; i++) {
            //loop through the table, find every active cell
            if(table[i] != null && table[i].active) {
                //get a new index for it in the new table
                newTableIndex = getIndexForKey(newTable, table[i].element, true);
                newTable[newTableIndex] = table[i]; //and put the entry in the table
                numOccupiedCells++; //we've occupied one more cell
            }
        }

        table = newTable; //finally, make our table point to the new table
    }

    /**
     * Finds and returns item from the set, or null if it's not in the set.
     * @param item The item to find and return
     * @return The found item, or null if nothing is found.
     */
    public Object find(Object item) {
        //don't try to find null items
        if(item == null) return null;
        //get the index, set stopOnInactive to false since the object
        //might be past the inactive value
        HashEntry hashEntry = table[getIndexForKey(table, item, false)];
        //if it didn't find it (null) or it's inactive, return null
        //to signal that we couldn't find it.
        if(hashEntry == null || !hashEntry.active) {
            return null;
        }
        //otherwise, return the element.
        return hashEntry.element;
        //note that hashEntry.element equals item, but they may have
        //different data, in the case of the Student class
        //(you can lookup by ID, and then get the associated lastName)
    }

    /**
     * Finds and deletes item from the set.
     * Does nothing if item is not in the set.
     * @param item The item to delete
     */
    public boolean delete(Object item) {
        if(item == null) return false;
        //find the value, like in the find method
        HashEntry hashEntry = table[getIndexForKey(table, item, false)];
        //if it found it (not null), set it to inactive.
        if(hashEntry != null && hashEntry.active) {
            hashEntry.active = false;
            return true;
        }
        return false;
    }

    //This is the method that does the hashing and quadratic probing.

    //curTable: the table to hash into. Usually just table, but sometimes
    //can be used with a different table, like in the rehash method.

    //key: the Object to hash. Can be used to find an available position for
    //this Object, or to find if the Object is already in the table.

    //stopOnInactive: Whether or not to stop probing when we find an inactive
    //cell. For example, when inserting, we want to stop and overwrite inactive
    //cells. For finding, our data may exist past an inactive cell.

    //Returns an index for curTable that has one of the following values:
    //null: The item was not found, but this is where it could be inserted
    //inactive cell: (if stopOnInactive is true) The item was found, but
    //is inactive.
    //active cell: The item was found here.

    private int getIndexForKey(HashEntry[] curTable, Object key,
     boolean stopOnInactive) {
        //get the position that it should be placed in before any probing
        int originalHash = Math.abs(key.hashCode()) % curTable.length;
        //hash will move around, but start it out at the optimal hash position
        int hash = originalHash;
        //for quadratic probing, we will increment this each time and then check
        //originalHash + quadraticCounter^2.
        int quadraticCounter = 1;

        //stop probing when any of the following occur:

        //We find an empty cell (null). This is useful for both the find and
        //insert methods. For find, it's the soonest we know that the element
        //is not in the table. For insert, it's where the item should be
        //inserted.

        //OR we hit an inactive cell AND we're supposed to stop on inactives
        //This is set to true on inserts, because we can overwrite inactives.

        //OR when we find an entry that matches our key. For find, this means
        //we've found our element. For insert, this means that the element
        //is already in the set. Each of these cases will be handled
        //by the calling method.
        while(
         curTable[hash] != null
         && (!stopOnInactive || curTable[hash].active)
         && !key.equals(curTable[hash].element)
        ) {
            //quadratic probe
            hash = (originalHash + quadraticCounter * quadraticCounter) % curTable.length;
            quadraticCounter++;
        }
        return hash;
    }

    /**
     * Prints the internal structure of the array used to hold the set.
     */
    public void printTable() {
        //loop through, print out info about each cell.
        for(int i = 0; i < table.length; i++) {
            System.out.print("[" + i + "]: " );
            if(table[i] == null) {
                System.out.println("empty");
            } else {
                System.out.println(table[i].element + ", " + (table[i].active ? "active" : "inactive"));
            }
        }
    }

    /**
     * Returns the number of elements in the set.
     * @return The number of elements in the set
     */
    public int elementCount() {
        int count = 0;
        for(int i = 0; i < table.length; i++) {
            if(table[i] != null && table[i].active) {
                count++;
            }
        }
        return count;
        //count all active entries
    }

    /**
     * Returns whether or not the set is empty.
     * @return true if the table is empty, false otherwise
     */
    public boolean isEmpty() {
        for(int i = 0; i < table.length; i++) {
            if(table[i] != null && table[i].active) {
                return false; //return false as soon
                //as we find an active element
            }
        }
        return true;
        //otherwise, return true, we went through the whole table
        //and there were no active cells
    }

    /**
     * Empties the set.
     */
    public void makeEmpty() {
        //reset the array, and reset the number of occupied cells
        table = new HashEntry[table.length];
        numOccupiedCells = 0;
    }

    /**
     * Prints the elements in the set.
     */
    public void outputData() {
        //use an iterator to print out each item.
        Iter iter = new Iter();
        while(iter.hasNext()) {
            Object item = iter.next();
            System.out.println(item + ", active");
        }
    }
}
