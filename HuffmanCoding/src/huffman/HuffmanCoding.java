package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.naming.ldap.SortResponseControl;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
        sortedCharFreqList = new ArrayList<CharFreq>();

        int[] arr = new int[128];
        int sum = 0;
        char newChar;
        int temp = 0;;
        while (StdIn.hasNextChar()){ // puts all char frequencies in arr 
            newChar = StdIn.readChar();
            int count = arr[newChar];
            arr[newChar] = count + 1;            
        }
        for ( int i = 0; i<arr.length; i++){ // sums array aka finds total number of characters (can be done with running (?) sum)
            sum += arr[i];
        }
        
        for (int i = 0; i < arr.length; i++){
            if (arr[i]!= 0){
                CharFreq charToInsert = new CharFreq((char) i, (double) arr[i] / sum);
                sortedCharFreqList.add(charToInsert);
                temp = i;
            }
        }
        if (sortedCharFreqList.size() == 1){
            if (temp == 127){
                sortedCharFreqList.add(new CharFreq((char) 0, 0));
            }
            else{
                sortedCharFreqList.add(new CharFreq((char) (temp + 1), 0));
            }
        }
        Collections.sort(sortedCharFreqList);

    /* Your code goes here */
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        TreeNode node1;
        TreeNode node2;
        TreeNode sumnode;
        // enqueueing the arrayList charfreq objects into the source queue
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();
        for (int i = 0; i < sortedCharFreqList.size(); i++){
            TreeNode treenode = new TreeNode(sortedCharFreqList.get(i), null, null);
            source.enqueue(treenode);
        }
        while (source.size() + target.size() > 1){ 
            if (target.isEmpty()){ // both nodes taken from source
                node1 = source.dequeue();
                node2 = source.dequeue();
            
            }
            else if (source.isEmpty()){
                node1 = target.dequeue();
                node2 = target.dequeue();
            }
            else if (source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()){ // source has the first smallest node
                node1 = source.dequeue();
                if (!source.isEmpty() && source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()){ // source has the second smallest node
                    node2 = source.dequeue();
                }
                else{ // target has the second smallest node
                    node2 = target.dequeue();
                }

            }
            else{// target has the first smallest node
                node1 = target.dequeue();
                    if (!target.isEmpty() && target.peek().getData().getProbOcc() < (source.peek().getData().getProbOcc())){ //target not null AND has the lower value
                        node2 = target.dequeue();
                    }
                    else{
                        node2 = source.dequeue();
                    }
            }
            sumnode = new TreeNode(new CharFreq(null, node1.getData().getProbOcc() + node2.getData().getProbOcc()), node1, node2);
            target.enqueue(sumnode);
        }
        huffmanRoot = target.peek();


    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128];
        imencoding(huffmanRoot, "");
    
  
    }
    
    private void imencoding(TreeNode root, String bitstring){// HELPER METHOD THAT I MADE UP 
            if (root.getLeft() == null && root.getRight() == null) {
                encodings[(int) root.getData().getCharacter()] = bitstring;
                return; 
            }
            imencoding(root.getLeft(), bitstring + "0");
            imencoding(root.getRight(), bitstring + "1");

    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String thestringwewant = "";
        while(StdIn.hasNextChar()){
           thestringwewant = thestringwewant + encodings[StdIn.readChar()];
        }
        writeBitString(encodedFile, thestringwewant);


    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        // 0 0 0 0 1 0 1 0 1 0 1 1 1 1 1 1 1 1 0 
        StdOut.setFile(decodedFile);
        String bitstring = readBitString(encodedFile);
        TreeNode ptr = huffmanRoot;
        String decodedtext = "";
            for (int i = 0; i < bitstring.length(); i++){
                    if (bitstring.charAt(i) == '0' && ptr.getLeft() != null) {ptr = ptr.getLeft();}
                    else if(bitstring.charAt(i) == '1' && ptr.getRight()!=null) {ptr = ptr.getRight();}
                    else{
                        decodedtext = decodedtext + ptr.getData().getCharacter();
                        ptr = huffmanRoot;
                        i--;
                    } 
            }
            decodedtext = decodedtext + ptr.getData().getCharacter();

        StdOut.print(decodedtext);
        
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}