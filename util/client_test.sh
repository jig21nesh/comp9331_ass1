#!/usr/bin/expect

cd /Users/jiggy/Development/GitRepos/COMP9331/ass1/comp9331_ass1/target/classes
# Set timeout for responses
set timeout -1

# Define the usernames and passwords
set usernames {"jiggy" "test" "a" "b" "c" "d" "e" "f"}
set passwords {"jiggy" "test" "a" "b" "c" "d" "e" "f"}

set basePort 35000

# Create a list to store the spawned processes
set processes {}

# Loop through each set of credentials
for {set i 0} {$i < [llength $usernames]} {incr i} {
    # Get the current username and password
    set username [lindex $usernames $i]
    set password [lindex $passwords $i]

    set port [expr {$basePort + $i}]

    # Run the Java program in the background
    spawn -noecho java org.example.client.Client localhost 60000 $port

    # Add the spawned process to the list
    lappend processes $spawn_id

    # Expect the "Username:" prompt and send the username
    expect "Username:"
    send -- "$username\r"

    # Expect the "Password:" prompt and send the password
    expect "Password:"
    send -- "$password\r"

    # Wait for program output and user input
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

set baseMessageIndex 1

for {set j 0} {$j < 10} {incr j} {
    set messageIndex [expr {$baseMessageIndex + $j}]

    # Loop through each user
    for {set i 0} {$i < [llength $usernames]} {incr i} {
        set currentUser [lindex $usernames $i]

        # Select a random target index (excluding the user's own index)
        set targetIndex $i
        while {$targetIndex == $i} {
            set randomIndex [expr {int(rand() * [llength $processes])}]
            set targetIndex [expr {($randomIndex + 1) % [llength $processes]}]
        }

        # Debug message to display the random values, target index, message index, user name, and process ID
        puts "User: ${currentUser} | Message Index: ${messageIndex} | Target Index: ${targetIndex} | Process ID: [lindex $processes $i]"

        # Send the "/msgto" command from the user to the selected target
        send -i [lindex $processes $i] "/msgto [lindex $usernames $targetIndex] Hello, this is a test message from ${currentUser} count ${messageIndex}\r"
        after 500
    }
}


# Wait for 1 second before sending the "/logout" command
after 1000

# Send the "/logout" command to all processes without checking the response
foreach pid $processes {
    send -i $pid "/logout\r"
}

# Wait for all spawned processes to finish
foreach pid $processes {
    expect -i $pid {
        eof {
            # Close the current program instance
            wait -i $pid
        }
    }
}
