#!/usr/bin/env bash

# Define Unicode characters for the tree structure
TREE_DIR="📁 "
TREE_FILE="📄 "
TREE_EDGE="└── "
TREE_PIPE="├── "
TREE_LINE│="│   "
TREE_BLANK="    "

# Core recursive function to traverse directories
print_tree() {
    local dir="$1"
    local prefix="$2"
    
    # Store directory contents in an array to safely handle spaces
    local items=("$dir"/*)
    
    # If the directory is empty, the array will contain a single non-existent path
    [[ ! -e "${items[0]}" ]] && return

    local count=${#items[@]}
    local index=0

    for item in "${items[@]}"; do
        ((index++))
        local base_name="${item##*/}"
        
        # Skip hidden files/directories (optional)
        [[ "$base_name" == .* ]] && continue

        # Determine which branch pointer to use
        local pointer=$TREE_PIPE
        local extension=$TREE_LINE│
        if (( index == count )); then
            pointer=$TREE_EDGE
            extension=$TREE_BLANK
        fi

        # Format output with icons based on type
        if [[ -d "$item" ]]; then
            echo -e "${prefix}${pointer}${TREE_DIR}${base_name}"
            print_tree "$item" "${prefix}${extension}"
        else
            echo -e "${prefix}${pointer}${TREE_FILE}${base_name}"
        fi
    done
}

# Use target directory or default to current directory
TARGET_DIR="${1:-.}"

# Strip trailing slash for consistency
TARGET_DIR="${TARGET_DIR%/}"

echo "🌴 ${TARGET_DIR}"
print_tree "$TARGET_DIR" ""
