// Empty:
prefix '' ds // res: L2003

// Invalid:
prefix 'ss' 'ss1' // res: L2005, L2005
prefix 'dasdsad' // res: L2005
prefix '\\\' // res: L2005



// Unclosed:
prefix 'dasdasd // res: L2007