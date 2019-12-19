import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Class that is used to perform Huffman compression on files
 * @author Uzoma Ohajekwe
 * @date 2019-03-19
 * @version 1.0
 */
public class HuffmanSubmit implements Huffman
{

    // Feel free to add more methods and variables as required.
    private final int REQUIRED_LENGTH_OF_BINARY_STRING = 8;
    public static void main(String[] args)
    {
        Huffman  huffman = new HuffmanSubmit();
        huffman.encode("alice30.txt", "alice.enc", "freq.txt");
        huffman.decode("alice.enc", "alice30_dec.txt", "freq.txt");
        // After decoding, both ur.jpg and ur_dec.jpg should be the same.
        // On linux and mac, you can use `diff' command to check if they are the same.
    }


    /**
     * Encodes the input file using Huffman Coding. Produces two files
     * @param inputFile The name of the input file to be encoded.
     *          Do not modify this file.
     *
     * @param outputFile The name of the output file  (after encoding)
     *                This would be a binary file.
     *                If the file already exists, overwrite it.
     *
     * @param freqFile  Stores the frequency of each byte
     *          This file is a text file
     *          where each row contains texual representation
     *          of each byte and the  number of occurence of this byte
     *          separated by ':'
     *          An example entry would look like:
     *          01100001:12345
     *          Which means
     *          the letter a (ascii code 097, binary representation 01100001)
     *          has occureed 12345. This file does not need to be sorted.
     *          If this file already exists, overwrite.
     */
    public void encode(String inputFile, String outputFile, String freqFile)
    {
        // TODO: Your code here

        BinaryIn bin = new BinaryIn(inputFile); //initializes reading of file
        int[] asc = new int[256]; //represents an array for all 256 ascii characters
        int count = 0; //variable used to count how many different characters are in binary input stream
        while(!bin.isEmpty())
        {
            char a = bin.readChar();
            //System.out.print(a);
            asc[a]++; //adds one to place of ascii index, effectively keeping frequency for any and every ascii character seen
            count++;
        }
        System.out.println();
        PriorityQueue<HuffTree<Character>> ht = new PriorityQueue<>();
        int numInitialTrees = 0;
        for (int i = 0; i < asc.length; i++)
        {
            if (asc[i] != 0) //if the frequency of a character does not equal 0, add that character as a tree to priority queue
            {
                ht.add(new HuffTree<Character>(new HuffNode<Character>((char)i, asc[i])));
                numInitialTrees++;
            }
        }

        for (int i = 1; i <= numInitialTrees-1; i++)
        {
            HuffTree<Character> x = ht.remove(); //removes the the smallest Huffman Tree i.e. one with lowest frequency
            HuffTree<Character> y = ht.remove(); //removes the second-smallest Huffman Tree
            HuffNode<Character> z = new HuffNode<Character>(x.root(), y.root(), (x.root().freq() + y.root().freq())); //creates new tree node that has pointers to smallest and second smallest trees in an interation and stores the sum of their frequencies as its own frequency
            ht.add(new HuffTree<Character>(z)); //inserts new Huffman Trie back into priority queue
        }
        ArrayList<CharObj> Alist = new ArrayList<>(); //creates new ArrayList to store members of charObj class
        String str = ""; //creates new string to contain codes for traversal
        encodingTraversal(ht.remove().root(), str, Alist); //see javadoc comments
        FileWriter fr;
        BufferedWriter br;
        try
        {
            fr = new FileWriter(freqFile);
            br = new BufferedWriter(fr);
            for (int i = 0; i < Alist.size(); i++)
            {
                String test = padBinaryString(Integer.toBinaryString(Alist.get(i).getElement()) + ":" + Alist.get(i).getFrequency());
                br.write(test);
                br.newLine();

            }
            br.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        BinaryOut ou = new BinaryOut(outputFile); //initializes BinaryOut object for encoding file
        BinaryIn inp = new BinaryIn(inputFile); //initializes BinaryIn object to read through input file to get characters
        while(!inp.isEmpty())
        {
            String center = search(Alist, inp.readChar()); //searches through array of char objects for the char object with the character that matches the current character read and returns the Huffman code for that character
            for (int k = 0; k < center.length(); k++) //8, Iterates through String center one char at a time, String center representing the Huffman code for the current character in input file, and writes depending on whether the char is 0 or 1, writes true or false to output file
            {
                if (center.charAt(k) == '0')
                {
                    ou.write(false);
                }
                else
                {
                    ou.write(true);
                }
            }
        }
        ou.close();
        ou.flush();

    }

    /**
     * Decodes the input file (which is the output of encoding())
     * using Huffman decoding.
     * @param inputFile The name of the input file to be decoded.
     *     Do not modify this file.
     *
     * @param outputFile The name of the output file  (after decoding)
     *
     * @param freqFile  freqFile produced after encoding.
     *     Do not modify this file.
     */
    public void decode(String inputFile, String outputFile, String freqFile)
    {
        // TODO: Your code here
        FileReader fw;
        BufferedReader br;
        int[] asc = new int[256]; //creates array used to store frequency of every ascii character
        int count = 0; //variable used to count how many different characters there are
        try
        {
            fw = new FileReader(freqFile); //creates new buffer reader ready to read frequency file
            br = new BufferedReader(fw);
            String st; //string used to read each line of frequency file
            while((st = br.readLine()) != null)
            {
                String[] de = st.split(":");
                asc[Integer.parseInt(de[0], 2)] = Integer.parseInt(de[1]);
                count++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String st; //string used to read each line of frequency file
        PriorityQueue<HuffTree<Character>> ht = new PriorityQueue<>();
        int numInitialTrees = 0;
        for (int i = 0; i < asc.length; i++)
        {
            if (asc[i] != 0)
            {
                ht.add(new HuffTree<Character>(new HuffNode<Character>((char) i, asc[i])));
                numInitialTrees++;
            }
        }

        for (int i = 1; i <= numInitialTrees-1; i++)
        {
            HuffTree<Character> x = ht.remove();
            HuffTree<Character> y = ht.remove();
            HuffNode<Character> z = new HuffNode<Character>(x.root(), y.root(), x.root().freq() + y.root().freq());
            ht.add(new HuffTree<>(z));
        }
        ArrayList<CharObj> ch = new ArrayList<>();
        String s = "";
        HuffTree<Character> finale = ht.remove();
        encodingTraversal(finale.root(), s, ch);
        BinaryIn bin = new BinaryIn(inputFile);
        BinaryOut ou = new BinaryOut(outputFile);
        HuffNode<Character> finale2 = finale.root();
        int aCount = finale.root.frequency;
        int i = 0;
        while (!bin.isEmpty())
        {
            while (!finale2.isLeaf() && !bin.isEmpty())
            {
                boolean bit = bin.readBoolean();
                if (!bit)
                {
                    finale2 = finale2.left();
                }
                else if (bit)
                {
                    finale2 = finale2.right();
                }
            }
            if(i<aCount){
                System.out.print(finale2.element);
                ou.write(finale2.element());}
            i++;
            finale2 = finale.root();

        }
        ou.close();
        ou.flush();
    }


    /**
     * Does a linear search through array of Huffman character object to find the character object containing the character matching the key, and returns the Huffman code of that character
     * @param arrl the array of Huffman character objects
     * @param key the character to be searched for
     * @return the Huffman code of the Huffman character object containing the character that matches the key
     */
    private String search(ArrayList<CharObj> arrl, char key)
    {
        for (int i = 0; i < arrl.size(); i++)
        {
            if (arrl.get(i).getElement() == key)
            {
                return arrl.get(i).getCode();
            }
        }
        return "";
    }

    /**
     * Recursively traverses through a Huffman Tree, finds and stores codes for all characters in the tree, using inorder traversal technique
     * @param rt the node currently being traversed through
     * @param s the String containing the current code, created as a result of either going left or right
     * @param c the array where objects containing character along with its Huffman code and frequency are stored
     */
    private void encodingTraversal(HuffNode rt, String s, ArrayList<CharObj> c)
    {
        if (rt == null)
        {
            return;
        }
        if (rt.left() != null)
        {
            s += "0"; //if the left root of the current node is not null, add 0 to the current node to signify moving left
        }
        encodingTraversal(rt.left(), s, c);
        if (rt.isLeaf())
        {
            c.add(new CharObj(rt.freq(), s, (char)rt.element())); //add a new new object containing a character, its code and frequency, and add that object to array if and only if current node is leaf (i.e. a node with a character element)
        }
        else
        {
            s = s.substring(0, s.length()-1); //if current node is not a root, take away the last digit of the code to signify moving back up from left node to parent node
            s += "1"; //add 1 to signify moving from parent node to right node

        }
        encodingTraversal(rt.right(), s, c);
        if (rt.right() != null)
        {
            s = s.substring(0, s.length()-1); //if inorder traversal of tree is done, remove the last node to signify moving from subtree right node to super tree
        }

    }

    /**
     * Pads binary string with 0s at the beginning if natural length of string is not 8
     * @param bi the binary string to be padded
     */
    private String padBinaryString(String bi) //fix magic number '8'
    {
        if (bi.length() == REQUIRED_LENGTH_OF_BINARY_STRING )
        {
            return bi;
        }

        String z = "";

        for (int i = 0; i < (REQUIRED_LENGTH_OF_BINARY_STRING - bi.length()) -1; i++)
        {
            z += "0";
        }
        z +=bi;
        return z;
    }

    /**
     * Class used to store information about a character for Huffman Encoding
     *
     * @author Uzoma Ohajekwe
     * @date 2019-03-19
     * @version 1.0
     */
    public class CharObj
    {
        /**
         * The frequency of a character for a given file
         */
        private int frequency;
        /**
         * The Huffman code used to represent the character made using a Huffman Tree
         */
        private String code;
        /**
         * The actual character
         */
        private char element;

        /**
         * Constructor for class CharObj
         * @param f the frequency of the character
         * @param c the Huffman code
         * @param e the character itself
         */
        public CharObj(int f, String c, char e)
        {
            frequency = f;
            code = c;
            element = e;
        }

        /**
         * Returns the Huffman code of this character
         * @return the Huffman code of this character
         */
        public String getCode()
        {
            return code;
        }

        /**
         * Returns the frequency of this character
         * @return the frequency of this character
         */
        public int getFrequency()
        {
            return frequency;
        }

        /**
         * Returns this character
         * @return this character
         */
        public char getElement()
        {
            return element;
        }
    }

    /**
     * Class that represents a node in a Huffman Tree. Inspired by Clifford A Shaffer's HuffBaseNode in Data Structures and Algorithm Analysis
     * @param <E> the Object the Ndde is
     */
    public class HuffNode<E>
    {
        /**
         * The left child of this node
         */
        private HuffNode left;
        /**
         * The right child of this node
         */
        private HuffNode right;
        /**
         * The frequency of this node
         */
        private int frequency;
        /**
         * The element contained in this node
         */
        private E element;

        /**
         * Two-args constructor for class HuffNode
         * @param newElement the element of the HuffNode
         * @param newFreq the frequency of the Huffnode
         */
        public HuffNode(E newElement, int newFreq)
        {
            left = null;
            right = null;
            element = newElement;
            frequency = newFreq;

        }

        /**
         * Three-args constructor of HuffNode
         * @param lf the left child of this node
         * @param rg the right child of this node
         * @param newFreq the frequency of this node
         */
        public HuffNode(HuffNode<E> lf, HuffNode<E> rg, int newFreq)
        {
            left = lf;
            right = rg;
            frequency = newFreq;
        }

        /**
         * Returns the right child of this HuffNode
         * @return the right child of this HuffNode
         */
        public HuffNode<E> right()
        {
            return right;
        }

        /**
         * Returns the left child of this HuffNode
         * @return the left child of this HuffNode
         */
        public HuffNode<E> left()
        {
            return left;
        }

        /**
         * Returns the element contained in this HuffNode
         * @return the element contained in this HuffNode
         */
        public E element()
        {
            return element;
        }

        /**
         * Returns the frequency of this HuffNode
         * @return the frequency of this HuffNode
         */
        public int freq()
        {
            return frequency;
        }

        /**
         * Determines and returns the truth value of whether this HuffNode is a leaf node or not
         * @return true if this HuffNode is a leaf node, false otherwise
         */
        public boolean isLeaf()
        {
            if (right() == null && left() == null)
            {
                return true;
            }
            return false;
        }
    }

    /**
     * Class that represents a Huffman Tree used for Huffman compression. Inspired by Clifford A Shaffer's HuffTree in "Data Structures and Algorithm
     * Analysis"
     * @param <E> the Object to be stored in the Huffman Tree
     *
     * @author Uzoma Ohajekwe
     * @version 1.0
     * @date 2019-03-19
     */
    public class HuffTree<E> implements Comparable<HuffTree<E>>
    {
        /**
         * The root of the tree
         */
        private HuffNode<E> root;

        /**
         * Default, no-args constructor for class HuffTree
         */
        public HuffTree()
        {
            root = null;
        }

        /**
         * Args constructor for class HuffTree
         * @param huff
         */
        public HuffTree(HuffNode<E> huff)
        {
            root = huff;
        }

        /**
         * Returns root node of Huffman Tree
         * @return the root node of Huffman Tree
         */
        public HuffNode<E> root()
        {
            return root;
        }

        /**
         * Sets the root node of the Huffman tree
         * @param co the new root of Huffman tree
         */
        public void setRoot(HuffNode<E> co)
        {
            root = co;
        }

        /**
         * Returns the frequency of the root
         * @return the frequency of the root
         */
        public int freq()
        {
            return root.freq();
        }

        /**
         * Compares this HuffTree to another HuffTree and returns either positive or negative number depending on whether this HuffTree is "larger" than argument HuffTree, or "smaller"
         * @param h2 the HuffTree to be compared to
         * @return 1 if this HuffTree is greater than arg HuffTree, 0 if they have the same frequency, -1, if this HuffTree has a lower frequency
         */
        public int compareTo(HuffTree<E> h2)
        {
            if (this.freq() > h2.freq())
            {
                return 1;
            }
            else if (this.freq() == h2.freq())
            {
                return 0;
            }
            return -1;
        }
    }
}
