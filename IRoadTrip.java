import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * The IRoadTrip class represents a program that calculates and displays the shortest path
 * between two countries based on geographical information such as borders and distances.
 * It reads data from input files, constructs an adjacency list, and performs Dijkstra's
 * algorithm to find the shortest path between two specified countries.
 *
 * The program takes three command-line arguments: borders file, capdist file, and state_name file.
 * It then processes these files to build the necessary data structures and performs user interactions
 * to find the shortest path between user-inputted countries.
 *
 * Note: The program expects specific formats for the input files to extract necessary information.
 */

public class IRoadTrip {

	private Map<String, Map<String, Integer>> adjacencyList; 
	
	private Map<String, String> stateNameMap;
	
	private Map<String, List<String>> aliasesMap;
	
	private Map<String, List<String>> countriesMap;
	
	private  Map<String, Integer> distances;
	
	private Map<String, String> previousVertices;


	/**
     * Constructs an IRoadTrip6 object and initializes data structures by reading input files.
     *
     * @param args Array of command-line arguments containing file names for borders, capdist, and state_name.
     */
    public IRoadTrip(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java IRoadTrip2 borders.txt capdist.csv state_name.tsv");
            System.exit(1);
        }

        adjacencyList = new HashMap<>();
        stateNameMap = new HashMap<>();
        countriesMap = new HashMap<>();
        aliasesMap = new HashMap<>();
        
        readStateNameFile(args[2]);       
        commaCheck();
        hyphenCheck();
        readBordersFile(args[0]);
        readCapDistFile(args[1]);
    }
    
    /**
     * Reads the state_name.tsv file and populates the stateNameMap and aliasesMap.
     *
     * @param fileName The name of the state_name.tsv file.
     */
    private void readStateNameFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
        	if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] fields = line.split("\t");

                String stateId = fields[1].trim();
                String countryName = fields[2].trim();
                String endDate = fields[4].trim();

                if (endDate.equals("2020-12-31")) {
                    List<String> aliases = bracketsCheck(countryName);
                    if(aliases.size() == 1) {
                    	aliases = slashCheck(countryName);
                    }
                    
                    aliasesMap.put(stateId, aliases);
                    stateNameMap.put(stateId, countryName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading state_name file: " + e.getMessage());
            System.exit(1);
        }
    }  
    
    /**
     * Reads the borders.txt file and populates the adjacencyList, aliasesMap and countriesMap.
     *
     * @param fileName The name of the borders.txt file.
     */
    private void readBordersFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                		
                String[] parts = line.split(" = ");
                String currentCountry = parts[0].trim();
                
                if (currentCountry.contains(" the")) {
                	currentCountry = currentCountry.replace("the", "").trim();
                }
                List<String> aliases = handleAliases(currentCountry);
                countriesMap.put(currentCountry, aliases);
                                
                String currentCountryId = getStateIdForBorders(currentCountry, aliases);
            	if (currentCountryId != null) {
            		if (aliasesMap.containsKey(currentCountryId)) {                        		
                    	for (String alias : aliases) {
                    		if(!aliasesMap.get(currentCountryId).contains(alias)) {
                    			aliasesMap.get(currentCountryId).add(alias);
                    		}                            		
                    	}                   	
                    } else {
                    	aliasesMap.put(currentCountryId, aliases);
                    }
                	
                    if (parts.length > 1) {
                    	
                    	adjacencyList.put(currentCountryId, new HashMap<>());
                    	
                    	String[] neighbors = parts[1].split(";");
                        for (String neighbor : neighbors) {
                            String[] neighborInfo = neighbor.split("\\d+", 2);
                            String neighborCountry = neighborInfo[0].trim();
                                                        
                            if (neighborCountry.contains(" the ")) {
                            	neighborCountry = neighborCountry.replace("the ", "").trim();
                            }
                            
                            List<String> neighborAliases = handleAliases(neighborCountry);

                            String neighborCountryId = getStateIdForBorders(neighborCountry, neighborAliases);                              
                            
                            if (neighborCountryId != null) {
                            	if (aliasesMap.containsKey(neighborCountryId)) {
                                	for (String neighborAlias : neighborAliases) {
                                		if(!aliasesMap.get(neighborCountryId).contains(neighborAlias)) {
                                			aliasesMap.get(neighborCountryId).add(neighborAlias);
                                		}                                       		
                                	}                   	
                                } else {
                                	aliasesMap.put(neighborCountryId, neighborAliases);
                                }                                                                       	
                        		adjacencyList.get(currentCountryId).put(neighborCountryId, Integer.MAX_VALUE);
                                                 		                                	
                              }                            	
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading borders file: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Reads the capdist.csv file and updates distances in the adjacencyList.
     *
     * @param fileName The name of the capdist.csv file.
     */
    private void readCapDistFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
        	if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        	while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String countryA = parts[1].trim();
                String countryB = parts[3].trim();
                int distance = Integer.parseInt(parts[4].trim());

                if (countryA.length() == 2) {
                	countryA = countryA + "G";
                }
                if (countryB.length() == 2) {
                	countryB = countryB + "G";
                }
                
                if (adjacencyList.containsKey(countryA) && adjacencyList.get(countryA).containsKey(countryB)) {
                    adjacencyList.get(countryA).put(countryB, distance);                    
                } 
            }
        } catch (Exception e) {
        	System.err.println("Error reading capdist file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Handles special cases for aliases with brackets and updates aliasesMap accordingly.
     *
     * @param country The country name to check for aliases with brackets.
     * @return A list of aliases for the given country.
     */
    private List<String> bracketsCheck(String country) {
    	List<String> parts = new ArrayList<>();
    	String[] split = country.split("\\)");
    	if(split.length == 1) {
    		String[] splitParts = country.split("\\(");
        	if(splitParts.length > 1) {
        		
        		parts.add(splitParts[0].trim());
        		parts.add(splitParts[1].substring(0, splitParts[1].length() - 1));
        		parts.add(country);
       		    
        	}
    	} else {
    		String[] splitParts = split[0].split("\\(");
    		if(splitParts.length > 1) {
    			parts.add(splitParts[0].trim() + " " + split[1].trim());
    			parts.add(splitParts[1].trim() + " " + split[1].trim());
    			parts.add(country);
  		
    		}
    	}
    	parts.add(country);
    	return parts; 
    	
    }
    
    /**
     * Handles special cases for aliases with slashes and updates aliasesMap accordingly.
     *
     * @param country The country name to check for aliases with slashes.
     * @return A list of aliases for the given country.
     */
    private List<String> slashCheck(String country) {
    	List<String> parts = new ArrayList<>();
    	String[] splitParts = country.split("/");
    	if(splitParts.length > 1) {
    		parts.add(splitParts[0]);
    		parts.add(splitParts[1]);
    	}else {
    		parts.add(country);
    	}
    	return parts;   	
    }
    
    /**
     * Checks and updates aliasesMap for aliases separated by commas.
     */
    private void commaCheck() {
    	for(String stateId : aliasesMap.keySet()) {
    		List<String> aliases = aliasesMap.get(stateId);
    		String[] splitParts = aliases.get(0).split(",\\s+");  
            if(splitParts.length > 1) {
            	aliases.add(splitParts[1].trim() + " " + splitParts[0].trim());
                aliasesMap.put(stateId, aliases);
            }
    	}
   }
    
    /**
     * Checks and updates aliasesMap for aliases separated by hyphens.
     */
    private void hyphenCheck() {
    	for(String stateId : aliasesMap.keySet()) {
    		List<String> aliases = aliasesMap.get(stateId);
    		String[] splitParts = aliases.get(0).split("-");
            if(splitParts.length > 1) {
            	 aliases.add(splitParts[0].trim() + " and " + splitParts[1].trim());
                 aliasesMap.put(stateId, aliases);
            }                
    	}
   }
    
    /**
     * Checks and updates aliasesMap for aliases separated by commas while reading borders file.
     *
     * @param country The country name to check for aliases with commas.
     * @return A list of aliases for the given country.
     */
    private List<String> commaCheckForBorders(String country) {
    	List<String> parts = new ArrayList<>();
    	String[] splitParts = country.split(",\\s+");
    	if(splitParts.length > 1) {
        	parts.add(country);
            parts.add(splitParts[0].trim());
            parts.add(splitParts[1].trim() + " " + splitParts[0].trim());
    	} else {        	
        	parts.add(country);        	
        }
    	return parts;
   }
    
    /**
     * Checks and updates aliasesMap for aliases with "and" while reading borders file.
     *
     * @param country The country name to check for aliases with "and".
     * @return A list of aliases for the given country.
     */
    private List<String> andCheck(String country) {
    	List<String> parts = new ArrayList<>();
    	String[] splitParts = country.split("\\s+and\\s+");             
        if(splitParts.length > 1) {
        	parts.add(country);
           parts.add(splitParts[0].trim() + "-" + splitParts[1].trim());
    	}  else {        	
       	parts.add(country);        	
       }
    	return parts;
   }
    
    /**
     * Handles different alias formats and returns a list of aliases for a given country.
     *
     * @param country The country name to check for aliases.
     * @return A list of aliases for the given country.
     */
    private List<String> handleAliases(String country) {
        List<String> parts = new ArrayList<>();
        
        if (country.matches(".*, [A-SU-Z].*")) {
        	String[] splitParts = country.split(",\\s+"); 
        	if(splitParts.length > 1) {
        		parts.add(country.trim());            
                parts.add(splitParts[1].trim() + " " + splitParts[0].trim());
        	}  
        } else if (country.contains("'")){        	
        	parts.add(country);
        	parts.add(convertLetterBeforeApostrophe(country));
        	
        } else {        	       	
        	parts = bracketsCheck(country);
        	if(parts.size() == 1) {
        		parts = commaCheckForBorders(country);
        		if(parts.size() == 1) {
        			parts = andCheck(country);
        		}
        	}        	
        }	
        return parts;   	
    }
   
    /**
     * Converts a country name(to get the alias) by converting the letter before an apostrophe to uppercase or lowercase.
     *
     * @param country The country name to convert.
     * @return The converted country name.
     */
    private String convertLetterBeforeApostrophe(String country) {
        int indexOfApostrophe = country.indexOf("'");
        if (indexOfApostrophe != -1 && indexOfApostrophe > 0) {
            char letterBeforeApostrophe = country.charAt(indexOfApostrophe - 1);
            char convertedLetter;

            if (Character.isUpperCase(letterBeforeApostrophe)) {
                convertedLetter = Character.toLowerCase(letterBeforeApostrophe);
            } else {
                convertedLetter = Character.toUpperCase(letterBeforeApostrophe);
            }

            return country.substring(0, indexOfApostrophe - 1) + convertedLetter + "’" + country.substring(indexOfApostrophe + 1);
        } else {
            return country;
        }
    }
    
    /**
     * Extracts uppercase letters from a string.
     *
     * @param input The input string.
     * @return Uppercase letters extracted from the input string.
     */
    private String extractUpperCaseLetters(String input) {
        String uppercaseLetters = "";
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                uppercaseLetters += c;
            }
        }
        return uppercaseLetters;
    }    
    
    /**
     * Determines the stateId for a given country name by checking aliasesMap while reading borders file.
     * 
     * While reading borders file need to update the aliasesMap if the country has aliases
     * but aliasesMap needs the stateId, so this function is created.
     *
     * @param countryName The country name to find the stateId for.
     * @param aliases A list of aliases for the country.
     * @return The stateId corresponding to the country name.
     */
    private String getStateIdForBorders(String countryName, List<String> aliases) {
    	
    	for(String alias : aliases) {
    		for (String stateId: aliasesMap.keySet()) {
    			List<String> aliasList = aliasesMap.get(stateId);
    			if (aliasList.contains(alias)) {
    				return stateId;
    			}
    		}	    			
    		
			for (String stateId: aliasesMap.keySet()) {
    			List<String> aliasList = aliasesMap.get(stateId);    			
    			
    			for (String s : aliasList ) {
    				if (s.contains(alias)) {
        				return stateId;
        			}
    				
    				String upperCaseLetters = extractUpperCaseLetters(s);
    				if (upperCaseLetters.equals(alias)) {
    					return stateId;    					
    				} else if (upperCaseLetters.substring(0, upperCaseLetters.length() - 1).equals(alias)) {
					aliasList.add(upperCaseLetters);
    					return stateId;
    				}
    				
    				if ((s + " ").contains(alias.substring(0, alias.length() - 1) + " ")) {
        				return stateId;
        			}				
    			}
    			
    			if (aliasList.contains(alias.substring(0, alias.length() - 1))) {
    				return stateId;
    			}
    			
    			String[] longName = alias.split(" ");
    			for(String longNamePart : longName) {
    				if (aliasList.contains(longNamePart)) {
					    return stateId;
				    }     				 
    			}
			}	
			
			 if (alias.equals("Romania") || alias.equals("Rumania")) {
	        	return "RUM";
	            
	        } else if (alias.equals("Kyrgyzstan") || alias.equals("Kyrgyz Republic")) {
	        	return "KYR";
	            
	        } else if (alias.equals("Czech Republic") || alias.equals("Czechia")) {
	        	return "CZR";
	        
	        } else if (alias.equals("Korea, South") || alias.equals("South Korea") || alias.equals("Korea, Republic of") || alias.equals("Republic of Korea")) {
	        	return "ROK";
	            
	        } else if (alias.equals("Korea, North") || alias.equals("North Korea") || alias.equals("Korea, People's Republic of") || alias.equals("People's Republic of Korea")) {
	        	return "PRK";
	            
	        } else if (alias.equals("Bahamas, The") || alias.equals("Bahamas") || alias.equals("The Bahamas")) {
	        	return "BHM";
	            
	        } else if (alias.equals("East Timor") || alias.equals("Timor-Leste")) {
	        	return "ETM";
	            
	        } else if (alias.equals("Cabo Verde") || alias.equals("Cape Verde")) {
	        	return "CAP";	            
	        } else if (alias.equals("Eswatini") || alias.equals("Swaziland")) {
	        	return "SWA";
	        }	
       }
    return null;
        
    }    
    
    /**
     * Retrieves the stateId for a given country name from aliasesMap.
     *
     * @param countryName The country name to find the stateId for.
     * @return The stateId corresponding to the country name.
     */
    private String getStateId(String countryName) {
    	for(String stateId : aliasesMap.keySet()) {
    		List<String> aliases = aliasesMap.get(stateId);
    		if (aliases.contains(countryName)) {
    			return stateId;
    		}
    	}
    	return null;
    }
    
    /**
     * Retrieves the country name for a given stateId from aliasesMap.
     *
     * @param stateId The stateId to find the country name for.
     * @return The country name corresponding to the stateId.
     */
    private String getCountryName(String stateId) {
    	if(aliasesMap.containsKey(stateId)) {
    		return aliasesMap.get(stateId).get(0);
    	}
        return "Unknown Country";
    }
    
    /**
     * Checks if a given country name is valid by searching aliasesMap and countriesMap.
     *
     * @param inputCountry The input country name to check.
     * @return True if the country name is valid, false otherwise.
     */
    private boolean isValidCountry(String inputCountry) {
    	String[] splitParts = inputCountry.split(" the ");
    	if(splitParts.length > 1) {
    		inputCountry = inputCountry.replace("the ", "").trim();
    	}
    	String[] splitParts1 = inputCountry.split(" ");
    	if(splitParts1[splitParts1.length - 1].equals("the")) {
    		inputCountry = inputCountry.replace("the", "").trim();
    	}
    	
    	for(String stateId : aliasesMap.keySet()) {
    		List<String> aliases = aliasesMap.get(stateId);
    		if (aliases.contains(inputCountry)) {
    			return true;
    		}
    	}	
    	for(String country : countriesMap.keySet()) {
    		List<String> countryAliases = countriesMap.get(country);
    		if (countryAliases.contains(inputCountry)) {
    			return true;
    		}
    	}
    	
        return false;
    }
   
    /**
     * Gets the distance between two countries using their stateIds.
     *
     * @param country1 The name of the first country.
     * @param country2 The name of the second country.
     * @return The distance between the two countries, or -1 if not found.
     */    
    public int getDistance (String country1, String country2) {
    	
    	String country1Id = getStateId(country1);
        String country2Id = getStateId(country2);

        if(country1Id == null || country2Id == null) {
        	return -1; 
        }
        if (adjacencyList.get(country1Id).containsKey(country2Id)) {
            return adjacencyList.get(country1Id).get(country2Id);
        } else {
            return -1;
        }
    }
       
    /**
     * Represents a node in the graph for Dijkstra's algorithm.
     */
    private class Node {
        String vertex;
        int distance;

        public Node(String vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    /**
     * Finds the shortest path between two countries using Dijkstra's algorithm.
     *
     * @param country1 The name of the starting country.
     * @param country2 The name of the destination country.
     * @return A list of steps in the shortest path.
     */
    public List<String> findPath(String country1, String country2) {
        List<String> path = new ArrayList<>();
        
        String country1Id = getStateId(country1);
        String country2Id = getStateId(country2);
       
        if (country1Id == null || country2Id == null || !adjacencyList.containsKey(country1Id) || !adjacencyList.containsKey(country2Id)) {
            return path;
        }

        distances = new HashMap<>();
        previousVertices = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));

        for (String vertex : adjacencyList.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE);
            previousVertices.put(vertex, null);          
        }

        distances.put(country1Id, 0);
        queue.add(new Node(country1Id, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (String neighbor : adjacencyList.get(current.vertex).keySet()) {
            	int alt = distances.get(current.vertex) + adjacencyList.get(current.vertex).get(neighbor);
            	if(adjacencyList.get(current.vertex).get(neighbor) == Integer.MAX_VALUE) {
            		alt = Integer.MAX_VALUE;
            		
            	}
                if (alt < distances.get(neighbor)) {
                    distances.put(neighbor, alt);
                    previousVertices.put(neighbor, current.vertex);
                    queue.add(new Node(neighbor, alt)); 
                }
            }
        }

        String current = country2Id;
        while (current != null) {
            path.add(0, current);
            current = previousVertices.get(current);
        }
        List<String> newPath = new ArrayList<>();
        for(String currentId : path) {
        	String currentName = getCountryName(currentId);
        	newPath.add(currentName);
        }
        List<String> convertedPath = convertList(newPath);

        return convertedPath;
    }
    
    /**
     * Converts a list of country names into a formatted list with distances between them 
     * for display purposes.
     *
     * @param originalList The original list of country names.
     * @return A formatted list with distances between countries.
     */
    private List<String> convertList(List<String> originalList) {
        List<String> convertedList = new ArrayList<>();

        for (int i = 0; i < originalList.size() - 1; i++) {
            String currentElement = originalList.get(i);
            String nextElement = originalList.get(i + 1);
            int distance = getDistance(currentElement, nextElement);

            String formattedElement = currentElement + " --> " + nextElement +" (" + distance + " km.)";
            convertedList.add(formattedElement);
        }

        return convertedList;
    }    
    
    /**
     * Accepts user input for country names and prints the shortest path between them.
     */
    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String country1;
            do {
                System.out.print("Enter the name of the first country (type EXIT to quit): ");
                country1 = scanner.nextLine().trim();

                if (country1.equalsIgnoreCase("EXIT")) {
                    scanner.close();
                    return; 
                }

                if (!isValidCountry(country1)) {
                    System.out.println("Invalid country name. Please enter a valid country name.");
                }
            } while (!isValidCountry(country1));

            String country2;
            do {
                System.out.print("Enter the name of the second country (type EXIT to quit): ");
                country2 = scanner.nextLine().trim();

                if (country2.equalsIgnoreCase("EXIT")) {
                    scanner.close();
                    return; 
                }

                if (!isValidCountry(country2)) {
                    System.out.println("Invalid country name. Please enter a valid country name.");
                }
            } while (!isValidCountry(country2));
            
            List<String> path = findPath(country1, country2);
           
            if (!path.isEmpty()) {
                System.out.println("Route from " + country1 + " to " + country2 + ":");
                for (String step : path) {
                    System.out.println("* " + step);
                }
            } else {
                System.out.println("No path found between " + country1 + " and " + country2);
            }
        }
    }
    
    /**
     * The main method that creates an instance of IRoadTrip and starts user input processing.
     *
     * @param args Command-line arguments containing file names.
     */
    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);
        
        a3.acceptUserInput();
    }

}
