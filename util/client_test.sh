#!/usr/bin/expect

# The script uses expect module for interacting with the client program.
# On macOS - install it via brew install expect
# On ubuntu - install it via sudo apt-get install expect

# for generating random number, I am using RAND() from TCL
# On macOS - install it via brew install tcl
# On ubuntu - install it via sudo apt-get install tcl

#Below is for Java only - change it to your own path where the classes are generated.
#Remove below line if required
cd /Users/jiggy/Development/GitRepos/COMP9331/ass1/comp9331_ass1/target/classes
# Set timeout for responses
set timeout -1

# Define the usernames and passwords
# Update the following two array based on your configuration. Keep in mind that the values are separated by space.

set usernames {"jiggy"  "a" "b" "c" "d"}
set passwords {"jiggy"  "a" "b" "c" "d"}

#This is the base UDP port. As you know each user will have to have a different UDP ports.
# So the logic is that 0th user will have the base port and then script adds one for each subsequent users.
# Ensure that you are not using any of these ports via different apps. If that is the case, just updated the
#blow value.
set basePort 35000

# Create a list to store the spawned processes
# Script uses these array to differentiate the users
set processes {}

# Loop through each set of credentials
for {set i 0} {$i < [llength $usernames]} {incr i} {
    # Get the current username and password
    set username [lindex $usernames $i]
    set password [lindex $passwords $i]

    set port [expr {$basePort + $i}]

    # Run the Java program in the background
    spawn -noecho java org.example.client.Client localhost 60000 $port

    #For Python
    #spawn -noecho python client.py $username $password $port

    # Add the spawned process to the list
    lappend processes $spawn_id

    # Expect the "Username:" prompt and send the username
    expect "Username:"
    send -- "$username\r"

    # Expect the "Password:" prompt and send the password
    expect "Password:"
    send -- "$password\r"

    # Keep the first line. Because it is as per the specs. i.e Welcome to Tessenger! is the first line of the output.
    # Remaining lines and the errors are based on your implementation. So you can update the below expect statements
    expect {
        "Welcome to Tessenger!" {
            # Program is running, you can add more interactions here if needed
        }
        "User is already logged in. Terminating this session" {
            # Program is already logged in, exit
            # Close the current program instance
            close
        }
        timeout {
            # Handle timeout or unexpected output if needed
            puts "Timeout or unexpected output occurred"
            exit 1
        }
    }
}

# Set the base message index. This is used to differentiate the messages sent by each user
# It is used in the /msgto command

set baseMessageIndex 1

# This is the most critical loop (or loops) for sending and testing the /msgto command
# The outer loop with the index J execute the message command
# the inner loop has some logic around picking the target user. Also, it has logic to check that
# if the random user is the same as the current user, then it will pick the next user in the list.
# puts command is just for debugging purpose. You can remove it if you want.
# Each time it waits for 500 millisecond to see the response from the client
# Don't worry about seeing messages here - it seems it is buffering before showing it on the console.

for {set j 0} {$j < 1} {incr j} {
    set messageIndex [expr {$baseMessageIndex + $j}]

    for {set i 0} {$i < [llength $usernames]} {incr i} {
        set currentUser [lindex $usernames $i]

        # Select a random target index (excluding the user's own index)
        set targetIndex $i
        while {$targetIndex == $i} {
            set randomIndex [expr {int(rand() * [llength $processes])}]
            set targetIndex [expr {($randomIndex + 1) % [llength $processes]}]
        }

        # Debug message remove if you like
        puts "User: ${currentUser} | Message Index: ${messageIndex} | Target Index: ${targetIndex} | Process ID: [lindex $processes $i]"

        send -i [lindex $processes $i] "/msgto [lindex $usernames $targetIndex] Hello, this is a test message from ${currentUser} count ${messageIndex}\r"
        after 500
    }
}


# Similar to the above loop, this loop is for creategroup command
# It picks the first 3 users and create a group with them. -- This is bit of hard coding. I haven't got chanced to make it more dynamic.
# i.e. user 0 will create a group with user 3,4,5. You can change it if you like.
for {set i 0} {$i < [llength $usernames]} {incr i} {
    if {$i == 0} {
      set groupMembers [join [list [lindex $usernames 3] [lindex $usernames 4] [lindex $usernames 5]] " "]
      set command "/creategroup testgroup $groupMembers\r"
      puts "Sending command: $command"
      send -i [lindex $processes $i] $command
      after 500
    }
}

# Similar to the above loop, this loop is for joingroup command
# As you can see the loop it from user count 3

for {set i 3} {$i < [llength $usernames]} {incr i} {
    if {$i >= 3 && $i <= 5} {
        send -i [lindex $processes $i] "/joingroup testgroup\r"
        after 500
    }
}

#Same as msgto loop but for the group message

set groupMsgContent "This is a group message."

for {set j 0} {$j < 10} {incr j} {


  for {set i 0} {$i < [llength $usernames]} {incr i} {
      set currentUser [lindex $usernames $i]

      send -i [lindex $processes $i] "/groupmsg testgroup $groupMsgContent from $currentUser count ${j}\r"
      after 500
  }
}


# Wait for 1 second before sending the "/logout" command
after 1000


#clean up the processes

foreach pid $processes {
    send -i $pid "/logout\r"
}

foreach pid $processes {
    expect -i $pid {
        eof {
            wait -i $pid
        }
    }
}
