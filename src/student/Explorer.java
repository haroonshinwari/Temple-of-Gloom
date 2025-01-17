package student;

import game.*;

import java.util.*;

public class Explorer {

    private Stack<Long> routeStack = new Stack<>();               // A stack keeping track of every move I make
    private List<Long> visitedTiles = new ArrayList<>();     // A list keeping track of every tile(node) I visit

    /**
     * Explore the cavern, trying to find the orb in as few steps as possible.
     * Once you find the orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb,
     * it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * <p>
     * At every step, you only know your current tile's ID and the ID of all
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles).
     * <p>
     * To get information about the current state, use functions
     * getCurrentLocation(),
     * getNeighbours(), and
     * getDistanceToTarget()
     * in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * <p>
     * Use function moveTo(long id) in ExplorationState to move to a neighboring
     * tile by its ID. Doing this will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     *
     * @param state the information available at the current state
     */
    public void explore(ExplorationState state) {

        routeStack.add(state.getCurrentLocation());                //add the current node to the stack

        while (state.getDistanceToTarget() != 0) {       // Explore keeps looping until distance to orb is 0 and then returns

            // add current tile to visited list, if it is not already in that list
            if (!visitedTiles.contains(state.getCurrentLocation())) {
                visitedTiles.add(state.getCurrentLocation());
            }

            boolean newTile = false;
            List<NodeStatus> nodes = (ArrayList) state.getNeighbours();       // a list holding all neighbours of the current tile
            long nextMove = 0;
            long moveId = nodes.get(0).getId();

            // if there is only one unvisited neighbour
            if (nodes.size() == 1 && !visitedTiles.contains(moveId)) {
                nextMove = moveId;
                newTile = true;
            }


            //if there is more than one neighbour, pick the closest one
            if (nodes.size() > 1){
                List<NodeStatus> tempneighbours = new ArrayList<>();
                for (NodeStatus n:nodes) {
                    if (!visitedTiles.contains(n.getId())) {
                        tempneighbours.add(n);
                    }
                }

                if (!tempneighbours.isEmpty()){

                    NodeStatus closest = tempneighbours.get(0);
                    for (NodeStatus cn : tempneighbours){
                        if (cn.getDistanceToTarget() < closest.getDistanceToTarget()){
                            closest = cn;
                        }
                    }
                    nextMove = closest.getId();
                    newTile = true;
                }

            }



            // if there is a neighbour, move the neighbour closest to the orb
            if (newTile == true) {
                state.moveTo(nextMove);
                routeStack.add(nextMove);
            }

            //if there are no new neighbours, move one step back
            if (newTile == false) {
                routeStack.pop();                                           // pop off the last current tile from stack
                state.moveTo(routeStack.peek());                            //move one step back
            }
        }
        return;
    }




    /**
     * Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph.
     * <p>
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * <p>
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold.
     *
     * @param state the information available at the current state
     */
    public void escape(EscapeState state) {
        Map<Node, Integer> totalDistance = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        List<Node> MinPQ = new ArrayList<>();
        HashSet<Node> visitedNodes = new HashSet<>();

        Node orbPosition = state.getCurrentNode();

        totalDistance.put(orbPosition, 0);              //giving the original starting tile a path length of 0
        MinPQ.add(orbPosition);                        // adding original position to the Priority Queue

        for (Node allNodes : state.getVertices()) {                 // giving all nodes a path lengeth of infinity
            if (allNodes != orbPosition) {
                totalDistance.put(allNodes, 999999999);
            }
        }

        while (!MinPQ.isEmpty()) {

            Node smallest = MinPQ.get(0);
            for (Node nn : MinPQ) {
                if (totalDistance.get(nn) < totalDistance.get(smallest)) {
                    smallest = nn;
                }
            }

            MinPQ.remove(smallest);
            visitedNodes.add(smallest);
            Node minNodeInPQ = smallest;           // takes the minimum element from the minPQ list

            Node exitNode = state.getExit();            // exit node to escape the temple
            if (minNodeInPQ.equals(exitNode)) {
                break;
            }

            int cumulativeWeight = totalDistance.get(minNodeInPQ);

            for (Edge neighboursEdge : minNodeInPQ.getExits()) {
                Node n = neighboursEdge.getOther(minNodeInPQ);         // returns node between the minPQ and edge
                if (!visitedNodes.contains(n)) {
                    int distanceViaN = cumulativeWeight + neighboursEdge.length();
                    int currentDistance = totalDistance.get(n);
                    if (currentDistance != 999999999) {
                        if (distanceViaN < currentDistance) {
                            totalDistance.put(n, distanceViaN);
                        }
                    } else {
                        totalDistance.put(n, distanceViaN);
                        MinPQ.add(n);
                    }
                    if (currentDistance != 999999999 || distanceViaN < currentDistance) {
                        previousNodes.put(n, minNodeInPQ);
                    }
                }
            }
        }


        List<Node> escapeRoute = new ArrayList<>();
        Node h = state.getExit();

        while (h != null) {
            escapeRoute.add(h);
            h = previousNodes.get(h);                           // collecting parent nodes from exit to orb location
        }
        Collections.reverse(escapeRoute);                       //reverse so the escape route is from orb to exit
        escapeRoute.remove(0);                           // remove the initial position of Phillip Hammond

        for (Node nextStep : escapeRoute) {
            if (state.getCurrentNode().getTile().getGold() > 0) {
                state.pickUpGold();
            }
            state.moveTo(nextStep);
        }

    }
}

