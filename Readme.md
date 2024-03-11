# Training data

My design for my training data was found after some experimentation with the ai and playing the game.  
I realized the game can be played if all you know is the row directly infront of the player. The reason for this  
is that if you know there is a block infront of you, you can just move out of the way and you will be ok each time the ship moves.  
To capture training data for this I first added time controls to the game pressing the number keys 1-4 changes the speed multiplier for the game.  
This allows me to capture training data much easier as I had to wait for the row infront of me to be blocked. I also made some modifications to the  
training data manually to remove duplicate data after that was done and fix any error. Along with that the data has some preprocessing done on  
it to optimize it for the neural network so that it can learn in a much quicker time than feeding it the whole play field.  
I have converted the model data to be the distance to the edge top and bottom this gives a much more simplified view for the neural network.
It allows for a much faster learning rate. I also normalize the data when loading it from the csv.

# Neural Network Topology & hyperparameters

Due to the optimization with the training data. I am able to get away with a much smaller network than I would have otherwise.  
My network consists of 3 layers. The input layer is 2 nodes. This is the distance to the edges of the play field.  
The hidden layer is 4 nodes as that appears to be the smallest size to guarantee a consistent output.  
The training function ResilientPropagation is used. The output layer is 1 node this is the direct output for the ship  
to move up, down or stay the same. I have a minimum error value of 0.001 which i check every iteration.  
I have a maximum of 1000 iterations in case the error never converges.  
I also used the TanH activation function for the hidden layer and the output layer.
