for(var i = 0; i < 6; i++)
	for(var i = 0; i < 5; i++)
		break

	if(i < 5)
		continue
	else
		for(var i = 0; i < 5; i++)
			break


while(true)
	while(true)
		break

	if(false)
		continue
	else
		while(true)
			break

for(val v in [ 1, 2, 3, 4 ] )
	while(true)
		break

	if(false)
		continue
	else
		while(true)
			break

/**/ continue // res: N2015
break // res: N2015

if(true)
	break // res: N2015
	continue // res: N2015
