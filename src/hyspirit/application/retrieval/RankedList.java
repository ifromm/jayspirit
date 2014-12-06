/*
 * Copyright 2000-2005 University Duisburg-Essen, Working group
 * "Information Systems"
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Created on 05-Jun-2005 16:19:29
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.2 $
 */
package hyspirit.application.retrieval;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.knowledgeBase.HyTupleFormatException;
import hyspirit.util.Util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Interface for ranked result lists. Elements of the lists are of type
 * hyspirit.application.retrieval.ResultItem. The ranked list is always
 * sorted in ascending order of the retrieval weights of its result items.
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 05-Jun-2005 16:19:29
 *
 */
public class RankedList {
	private final List<ResultItem> ranking = new Vector<ResultItem>();
    private boolean isSorted = false;
    private int nextElement = 0;
    
    private HashSet<String> whitelist = null;
    
    /**
     * Gets the whole ranked list, sorted in ascending order of the retrieval
     * weight of its result item.
     * @return the ranked list
     */
	public List<ResultItem> getList() {
        if (ranking.size() > 0) {
            sort();
            return ranking;
        }
        else return null;
    }
   
    /**
     * Gets the next <number> elements of the list. Returns null if there are
     * no next elements. If less than number elements are left, only these are
     * returned.
     * @param number the number of next elements
     * @return a subset of the ranked list or null
     */
	public List<ResultItem> getNextElements(int number) {
		Vector<ResultItem> nextElems = null;
        if (number > 0 && nextElement < ranking.size()) {
			nextElems = new Vector<ResultItem>(number);
            for (int i = nextElement;
            	(i < nextElement + number && i < ranking.size());
            	i++) {
                nextElems.add(ranking.get(i));
            }
            nextElement += number;
        }
        return nextElems;
    }
   
	/**
	 * Returns the next element in the list that has not been returned yet.
	 * 
	 * @return
	 */
	public ResultItem getNextElement() {
		if (hasNext()) return ranking.get(nextElement++);
		else return null;
	}

    /**
     * Gets the first <number> elements of the list. Returns null if there are
     * no next elements. If less than number elements are left, only these are
     * returned.
     * @param number the number of next elements
     * @return a subset of the ranked list or null
     */
	public List<ResultItem> getFirstElements(int number) {
		Vector<ResultItem> elems = new Vector<ResultItem>(number);
        for (int i = nextElement;
             (i < number && i < ranking.size());
             i++) elems.add(ranking.get(i));
        return elems;
    }
    
    /**
     * Returns true if there are next elements to fetch, false otherwise
     * @return true or false
     */
    public boolean hasNext() {
        return (nextElement < ranking.size());
    }
    
    /**
     * Returns the number of elements left in the ranking (i.e. fetchable with
     * getNextElements())
     * 
     * @return the number of elements left
     */
    public int elementsLeft() {
        return ranking.size() - nextElement;
    }
    
    /**
     * Resets the internal counter for getNextElements() to 0.
     */
    public void reset(){
        nextElement = 0;
    };
    
   /**
    * The number of elements in the ranking
    * @return the size of the ranking
    */
    public int size() {
        return ranking.size();
    }
    
    
    /**
     * Returns a range of the ranked list (including begin and end element). If
     * the end index is greater than the index of the last element, all items
     * up to the last element are returned.
     * @param begin the beginning of the range (>= 1)
     * @param end the end of the range
     * @return a subset of the ranked list or null if there is no element
     * 		within the given range
     */
	public List<ResultItem> getRange(int begin, int end)
    {
        begin--;
        end--;
		Vector<ResultItem> range = null;
        if (begin < ranking.size()) {
			range = new Vector<ResultItem>();
            for (int i = begin;
 (i <= begin + end && i < ranking.size()); i++)
                range.add(ranking.get(i));
        }
        return range;
    }
    
    
    /**
     * Add a new result item to the ranking. This invokes the reset() method.
     * @param res the result item to add
     */
    public void add(ResultItem res){
        if (whitelist == null || whitelist.contains(res.getURI())) {
            ranking.add(res);
            isSorted = false;
            reset();
        }
    }
    
    
    /**
	 * Sort the ranking. This method can be invoked explicitly, or is invoked
	 * when one of the get methods is called and the current list is not sorted.
	 */
    public void sort() {
        if (!isSorted) {
			Collections.sort(ranking);
            isSorted = true;
        }
    }

    /**
     * Parses the output of a hy_{pra|prd|fvpd} run and converts it into a
     * ranked list. A HySpirit result looks like this:
     * 
     *   0.320706 ("http://news.zdnet.com/2100-3513_22-5535433.html")
     *   0.305206 ("http://news.zdnet.com/2100-1009_22-5535228.html")
     *
     * @param hySpiritResult the HySpirit result
     */
    public void parseHySpiritResult(String hySpiritResult)
    throws HyTupleFormatException {
        if (hySpiritResult != null && !hySpiritResult.equals("")) {
            BufferedReader s = new BufferedReader(
                    			new StringReader(hySpiritResult));
            try{
                String line = null;
                while ((line = s.readLine()) != null)
                    add(new ResultItem(new HyTuple(line)));
                sort();
            }
            catch (IOException io) {
                io.printStackTrace(System.err);
            }
        }
    }
    
    /**
     * Parses the output of a hy_{pra|prd|fvpd} run and converts it into a
     * ranked list. A HySpirit result looks like this:
     * 
     *   0.320706 ("http://news.zdnet.com/2100-3513_22-5535433.html")
     *   0.305206 ("http://news.zdnet.com/2100-1009_22-5535228.html")
     *
     * @param hySpiritResult the HySpirit result as a Vector
     */
	public void parseHySpiritResult(Vector<String> hySpiritResult) {
        if (hySpiritResult != null) {
			Iterator<String> it = hySpiritResult.iterator();
            while (it.hasNext()) {
				String line = it.next();
                String[] tokens = line.split(" ");
                // The first token is the string representation of the
                // probabilistic weight; all other tokens are the retrieved
                // item and must be reconstructed.
                float weight = Float.parseFloat(tokens[0]);
                String item = "";
                for (int i = 1; i < tokens.length; i++)
                        item += tokens[i] + " ";
                add(new ResultItem(weight, item));
             }
             sort();
        }
    }

    
    /**
     * A whitelist is a list of URIs you want to see in the ranking. Any item
     * not present in the whitelist will be deleted from the ranking. This is
     * useful for filtering your results and more efficient as a PD join like
     * <code>
     * ?- term(T,D) & document(D)
     * </code>
     * In the above case, you should create a whitelist containing the URIs of
     * all documents, run the query
     * <code>
     * ?- term(T,D)
     * </code>
     * and only documents fulfilling the query will be returned.
     * 
     * @param whitelist
     */
    public void setWhitelist(HashSet<String> whitelist) {
        this.whitelist = whitelist;
    }
    
    
    /**
     * Writes the ranked list into a TREC result file
     * @param filename the fiolename of the TREC result file
     * @param queryID the TREC query ID
     * @param runID the TREC run ID
     * @param limit how many resulkt items to write; 0 for unlimited
     * @param append whether to append the result to an existing file
     * @param stripQuotationMarks whether or not to strip quotation marks from
     *        result URIs
     * @throws IOException
     */
    public void writeTRECResult(String filename,
                                String queryID,
                                String runID,
                                int limit,
                                boolean append,
                                boolean stripQuotationMarks) 
    throws IOException {
        FileWriter fw = new FileWriter(filename, append);
        sort();
		List<ResultItem> resultlist = limit > 0 ? getFirstElements(limit)
                                        : getList();
        if (resultlist != null) {
            for (int i = 1; i <= resultlist.size(); i++) {
                ResultItem rItem = resultlist.get(i-1);
                String uri = rItem.getURI();
                if (stripQuotationMarks) {
                    if (uri.startsWith("\"") && uri.endsWith("\""))
                        uri = uri.substring(1, uri.length()-1);
                }
                fw.write(queryID + " Q0 "
                       + uri + " "
                       + i + " "
                       + rItem.rsvString() + " "
                       + runID + "\n");
            }            
        }
        fw.close();
    }
    
    
    public static void main(String[] args) {
        System.out.println("RankedList $Revision: 1.2 $\n");
        System.out.println("1 -- Test ranking");
        System.out.println("2 -- HySpiritOutput to RankedList");
        System.out.print("\nPlease choose: ");
		String input = Util.readln();
		if(input.equals("1")) {
		    ResultItem i1 = new ResultItem(0.35f, "d1");
		    ResultItem i2 = new ResultItem(0.45f, "d2");
		    ResultItem i3 = new ResultItem(0.03f, "d3");
		    ResultItem i4 = new ResultItem(0.134f, "d4");
		    ResultItem i5 = new ResultItem(0.35f, "d5");
		    
		    RankedList ranking = new RankedList();
		    ranking.add(i1);
		    ranking.add(i2);
		    ranking.add(i3);
		    ranking.add(i4);
		    ranking.add(i5);
		    ranking.sort();
		    
		    System.out.println("The whole ranking:");
			List<ResultItem> rankedList = ranking.getList();
		    if (rankedList != null) {
		        for (int i = 0; i < rankedList.size(); i++) {
		            ResultItem rItem = rankedList.get(i);
		            System.out.println(rItem.getRSV() + "\t" + rItem.getURI());
		        }
		    }
		    
		    System.out.println("\nFirst two elements:");
		    rankedList = ranking.getNextElements(2);
		    if (rankedList != null) {
		        for (int i = 0; i < rankedList.size(); i++) {
		            ResultItem rItem = rankedList.get(i);
		            System.out.println(rItem.getRSV() + "\t" + rItem.getURI());
		        }
		        if (ranking.hasNext())
		            System.out.println(ranking.elementsLeft() + " items left.");
		    }
		    while ((rankedList = ranking.getNextElements(2)) != null) {
		        System.out.println("\nNext two elements:");
		        for (int i = 0; i < rankedList.size(); i++) {
		            ResultItem rItem = rankedList.get(i);
		            System.out.println(rItem.getRSV() + "\t" + rItem.getURI());
		        }
		        if (ranking.hasNext())
		            System.out.println(ranking.elementsLeft() +
		                    " item(s) left.");
		    }
		    
		    System.out.println("\nRange 1-3:");
		    rankedList = ranking.getRange(1,3);
		    if (rankedList != null) {
		        for (int i = 0; i < rankedList.size(); i++) {
		            ResultItem rItem = rankedList.get(i);
		            System.out.println(rItem.getRSV() + "\t" + rItem.getURI());
		        }
		    }       
		    System.out.println("\nRange 4-7:");
		    rankedList = ranking.getRange(4,7);
		    if (rankedList != null) {
		        for (int i = 0; i < rankedList.size(); i++) {
		            ResultItem rItem = rankedList.get(i);
		            System.out.println(rItem.getRSV() + "\t" + rItem.getURI());
		        }
		    }
		}
		else if (input.equals("2")) {
		    System.out.println("\nPlease input the HySpirit result " +
		            	"(insert empty line when finished):");
		    String hyspiritResult = "";
		    while (!((input = Util.readln()).equals(""))) {
		        hyspiritResult += input + "\n";
		    }
		    RankedList ranking = new RankedList();
		    ranking.parseHySpiritResult(hyspiritResult);
			List<ResultItem> rankedList = ranking.getList();
		    if (rankedList != null) {
		        for (int i = 0; i < rankedList.size(); i++) {
		            ResultItem rItem = rankedList.get(i);
		            System.out.println(rItem.getRSV() + "\t" + rItem.getURI());
		        }
		    }       		    
		    
		}
		else System.out.println("Goodbye! :-)");
    }
}
