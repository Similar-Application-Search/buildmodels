# 	modified by Chengjun Yuan <cy3yb@virginia.edu>
#	April 11th 2016
#	input: 	word2vec.model  
#			docForWmd.txt
#	output:	distanceMatrix.csv
#	Usage: python querySimilarProjectsInWmd.py word2vec.model docForWmd.txt projectDetails.txt queryProjectName rankedSimilarProjects.csv
# 	modified by Md Masudur Rahman <mr5ba@virginia.edu>
import gensim, pdb, sys, scipy.io as io, numpy as np, pickle, string, multiprocessing as mp
sys.path.append('python-emd-master')
from emd import emd
import datetime

X = 0
BOW_X = 0
queryIndex = 0
# read datasets line by line
def read_line_by_line(dataset_name,C,model,vec_size):
	# get stop words (except for twitter!)
	wmdWords = []
	SW = set()
	for line in open('stop_words.txt'):
		line = line.strip()
		if line != '':
			SW.add(line)

	stop = list(SW)
	f = open(dataset_name)
	if len(C) == 0:
		C = np.array([], dtype=np.object)
	num_lines = sum(1 for line in open(dataset_name))
	y = np.zeros((num_lines,))
	X = np.zeros((num_lines,), dtype=np.object)
	BOW_X = np.zeros((num_lines,), dtype=np.object)
	count = 0
	remain = np.zeros((num_lines,), dtype=np.object)
	the_words = np.zeros((num_lines,), dtype=np.object)
	lineCount=0
	for line in f:
		#print '%d out of %d' %(count+1, num_lines)
		#print count, num_lines
		line = line.strip()
		T = line.split('\t', 1)
		
		classID = T[0]
		if classID in C:
			IXC = np.where(C==classID)
			y[count] = IXC[0]+1
		else:
			C = np.append(C,classID)
			y[count] = len(C)

		W = T[1].translate(string.maketrans("",""), string.punctuation).split()
		#W = T[1].split()
		wmdWords.append(T[1].translate(string.maketrans("",""), string.punctuation))

		F = np.zeros((vec_size,len(W)))
		inner = 0
		RC = np.zeros((len(W),), dtype=np.object)
		word_order = np.zeros((len(W)), dtype=np.object)
		bow_x = np.zeros((len(W),))
		for word in W[0:len(W)]:
			try:
				test = model[word]
				if word in stop:
					word_order[inner] = ''
					continue
				if word in word_order:
					IXW = np.where(word_order==word)
					bow_x[IXW] += 1
					word_order[inner] = ''
				else:
					word_order[inner] = word
					bow_x[inner] += 1
					F[:,inner] = model[word]
			except KeyError, e:
				#print 'Key error: "%s"' % str(e)
				word_order[inner] = ''
			inner = inner + 1
		Fs = F.T[~np.all(F.T == 0, axis=1)]
		word_orders = word_order[word_order != '']
		bow_xs = bow_x[bow_x != 0]

		#print bow_x, bow_xs

		X[count] = Fs.T
		the_words[count] = word_orders
		BOW_X[count] = bow_xs
		count = count + 1;
		lineCount = lineCount + 1
		#if lineCount%10000 == 0:
			#print 'Line load completed = ',lineCount
	#print 'Class: ', C
	#print 'create X, BOW_X, C finished ..'
	return X, BOW_X, y, C, the_words, wmdWords

def distance(x1,x2):
	return np.sqrt( np.sum((np.array(x1) - np.array(x2))**2) )

def get_wmd(ix):
	n = np.shape(X)
	n = n[0]
	Di = np.zeros((1,n))
	i = ix
	#print '%d out of %d' % (i, n)
	for j in xrange(i):
		Di[0,j] = emd( (X[i], BOW_X[i]), (X[j], BOW_X[j]), distance)
	return Di 

def getSingleWmd(ix):
	#print '%d out of n' % ix
	Di = 0.0
	if ix != 0:
		#print 'X1[3]=', X1[2]
		#print 'BOW_X1[3]', BOW_X1[2]
		#Di = emd((X[ix], BOW_X[ix]), (X1[5], BOW_X1[5]), distance)
		Di = emd((X[ix], BOW_X[ix]), (X1[queryIndex], BOW_X1[queryIndex]), distance)
	#print 'Di is=: ', Di 
	return Di

def readProjectDetails(projectDetailsFile):
	projectDetails = []
	projectName = []
	for line in open(projectDetailsFile):
		if '\t' in line:
			lineTokens = line.split('\t', 1)
			projectDetails.append(lineTokens[1])
			projectName.append(lineTokens[0])
		else:
			print 'no tap space: ', line
	return projectName, projectDetails

def main():
	global X, BOW_X, queryIndex
	global X1, BOW_X1
	if len(sys.argv) != 13:
		print "Usage: python querySimilarProjectsInWmd.py word2vec.model trainDocForWmd.txt trainProjectDetails.txt testDocForWmd.txt testProjectDetails.txt searchResults"
		sys.exit()
	#print 'Start Loading word2vec.model',datetime.datetime.now()	
	# 0. specify train/test datasets
	word2vec_model = sys.argv[1] # e.g.: 'GoogleNews-vectors-negative300.bin'
	train_dataset = sys.argv[2] # e.g.: 'twitter.txt'
	trainProjectDetailsFile = sys.argv[3]
	trainProjetGitURLFile = sys.argv[4]
	trainProjetCategoryFile = sys.argv[5]

	test_dataset = sys.argv[6]
	testProjectDetailsPath	 = sys.argv[7]
	testProjetGitURLpath = sys.argv[8]
	testProjetCategorypath = sys.argv[9]

	trainVectorPK	 = sys.argv[10]
	testVectorPK	 = sys.argv[11]
	save_file_search_results	 = sys.argv[12]
	#save_file_mat = sys.argv[3] # e.g.: 'twitter.mat'

	trainProjectName,trainProjectDetails = readProjectDetails(trainProjectDetailsFile)
	testProjectName,testProjectDetails = readProjectDetails(testProjectDetailsPath)

	trainProjectURName,trainProjectGitURL = readProjectDetails(trainProjetGitURLFile)
	testProjectURName,testProjectGitURL = readProjectDetails(testProjetGitURLpath)

	trainProjectCategoryName,trainProjectCategory = readProjectDetails(trainProjetCategoryFile)
	testProjectCategoryName,testProjectCategory = readProjectDetails(testProjetCategorypath)
	# 1. load word2vec model (trained on Google News)
	#model = gensim.models.Word2Vec.load_word2vec_format('GoogleNews-vectors-negative300.bin', binary=True)
	if word2vec_model.endswith('.bin') :
		model = gensim.models.Word2Vec.load_word2vec_format(word2vec_model, binary=True)
	else :
		model = gensim.models.Word2Vec.load(word2vec_model)
	#print 'End Loading word2vec.model',datetime.datetime.now()	
	vec_size = 300


	#print 'Start Loading vectors.pk',datetime.datetime.now()
	with open(trainVectorPK) as f:
		[X, BOW_X, y, C, words] = pickle.load(f)
	#print 'End Loading vectors.pk',datetime.datetime.now()
	n = np.shape(X)
	n = n[0]
	D = np.zeros((n,n))
	for i in xrange(n):
		bow_i = BOW_X[i]
		bow_i = bow_i / np.sum(bow_i)
		bow_i = bow_i.tolist()
		BOW_X[i] = bow_i
		X_i = X[i].T
		X_i = X_i.tolist()
		X[i] = X_i

	#print 'Start loading vectors1.pk',datetime.datetime.now()
	#Test data
	with open(testVectorPK) as f:
		[X1, BOW_X1, y1, C1, words1] = pickle.load(f)
	#print 'End loading vectors1.pk',datetime.datetime.now()
	n1 = np.shape(X1)
	n1 = n1[0]
	D1 = np.zeros((n1,n1))
	for i in xrange(n1):
		bow_i1 = BOW_X1[i]
		bow_i1 = bow_i1 / np.sum(bow_i1)
		bow_i1 = bow_i1.tolist()
		BOW_X1[i] = bow_i1
		X_i1 = X1[i].T
		X_i1 = X_i1.tolist()
		X1[i] = X_i1

	n = np.shape(X)
	n = n[0]
	#print 'testProjectDetails size = ',len(testProjectDetails)
	#print 'Start evaluation at ',datetime.datetime.now()
	#print 'save_file_search_results',save_file_search_results
	#get stat from category data on training data
	category_stats = {}
	with open(trainProjetCategoryFile) as fc:

	    for line in fc:
	        pname,pcategory = line.split("\t")
	        pcategory = pcategory.replace("\n","")
	        #print pcategory
	        count_key = category_stats.get(pcategory,0)#if not found will return zero
	        category_stats[pcategory]=int(count_key) + 1
	#print category_stats
	#print category_stats.get('MUSIC_AND_AUDIO:Music & Audio',0)  
	f = open(save_file_search_results + ".txt", "w")
	f.write("Rank"+"\t"+"Project Name"+"\t"+"Project Description"+"\t"+"Github URL"+"\t"+"Category"+"\t"+"Judgement(0-5)"+"\n")
	#print "Rank"+"\t"+"Project Name"+"\t"+"Project Description"+"\t"+"Github URL"+"\t"+"Category"+"\t"+"Judgement(0-5)"+"\n"
	MAP=0
	MapAt5=0
	MapAt1=0
	MapAt3=0

	countQuery=0
	for projectIndex in xrange(0,len(testProjectDetails)):
		#print "Entering"
		queryIndex=projectIndex
		pool = mp.Pool(processes=14)
		#print 'Start pooling at ',datetime.datetime.now()
		pool_outputs = pool.map(getSingleWmd, list(range(n)))
		#pool_outputs = pool.map(query, list(range(n)))
		pool.close()
		pool.join()
		#print 'End pooling at ',datetime.datetime.now()
		distances = []
		for i in range(len(pool_outputs)):
			distances.append((i, pool_outputs[i]))

		distances.sort(key = lambda x : x[1])
		
		#f = open(queryProjectName + ".txt", "w")
		#print 'Distance len = ',len(distances)
		#f.write("*****************************************"+"\n")
		#for i in range(0,len(distances)):
		#	print distances[i][0]
		#print ("C1[length] = "+str(len(C1)))
		topN = 10
		avgp=0.0
		avgpAt5 = 0
		avgpAt1 = 0
		avgpAt3 = 0
		countRelavance=0
		true_relevane = category_stats.get(testProjectCategory[queryIndex].replace("\n",""),0) # minimum between n and total relevance document, n is top 
		totalCategoryRelevance= min(topN, true_relevane)
		if totalCategoryRelevance>0:
			countQuery=countQuery+1 #include this query in the MAP calculation
		f.write("Rank"+"\t"+"Search Project: "+testProjectName[queryIndex]+"\t"+testProjectDetails[queryIndex].replace("\n","")+"\t"+testProjectGitURL[queryIndex].replace("\n","")+"\t"+testProjectCategory[queryIndex])
		#print ("Rank"+"\t"+"Search Project: "+testProjectName[queryIndex]+"\t"+testProjectDetails[queryIndex].replace("\n","")+"\t"+testProjectGitURL[queryIndex].replace("\n","")+"\t"+testProjectCategory[queryIndex])
		for i in range(1,len(distances)):
			#print ("i is = "+str(i))
			f.write(str(i) + "\t" + trainProjectName[distances[i][0]]+"\t"+trainProjectDetails[distances[i][0]].replace("\n","")+"\t"+trainProjectGitURL[distances[i][0]].replace("\n","")+"\t"+trainProjectCategory[distances[i][0]]) 
			if(testProjectCategory[queryIndex]==trainProjectCategory[distances[i][0]]):
				countRelavance = countRelavance + 1
				avgp = avgp + (countRelavance*1.0)/i;
				if i<=5:
					avgpAt5 = avgpAt5 + (countRelavance*1.0)/i;
				if i<=3:
					avgpAt3 = avgpAt3 + (countRelavance*1.0)/i;
				if i<=1:
					avgpAt1 = avgpAt1 + (countRelavance*1.0)/i;

			if i>=10:
				break
		if totalCategoryRelevance>0:
			avgp = (avgp*1.0)/totalCategoryRelevance
			f.write("Average Prevision@10 = "+str(avgp)+"\n")
			#print ("AVG@10"+str(avgp)+"\t" +str(true_relevane)+"\n")
			MAP = MAP + avgp
		else:
			f.write("No relevance judgement for this query category. Average Prevision = none \n")
		totalCategoryRelevance_at_5 = min(5, true_relevane)
		if totalCategoryRelevance_at_5>0:
			avgpAt5 = (avgpAt5*1.0)/totalCategoryRelevance_at_5
			f.write("Average Prevision@5 = "+str(avgpAt5)+"\n")
			#print ("AVG@5"+str(avgpAt5)+"\t" +str(true_relevane)+"\n")
			MapAt5 = MapAt5 + avgpAt5
		else:
			f.write("No relevance judgement for this query category. Average Prevision = none \n")
		totalCategoryRelevance_at_1 = min(1, true_relevane)
		if totalCategoryRelevance_at_1>0:
			avgpAt1 = (avgpAt1*1.0)/totalCategoryRelevance_at_1
			f.write("Average Prevision@1 = "+str(avgpAt1)+"\n")
			#print ("AVG@1"+str(avgpAt1)+"\t" +str(true_relevane)+"\n")
			MapAt1 = MapAt1 + avgpAt1
		else:
			f.write("No relevance judgement for this query category. Average Prevision = none \n")
	   	
	   	totalCategoryRelevance_at_3 = min(3, true_relevane)
		if totalCategoryRelevance_at_3>0:
			avgpAt3 = (avgpAt3*1.0)/totalCategoryRelevance_at_3
			f.write("Average Prevision@3 = "+str(avgpAt3)+"\n")
			#print ("AVG@1"+str(avgpAt1)+"\t" +str(true_relevane)+"\n")
			MapAt3 = MapAt3 + avgpAt3
		else:
			f.write("No relevance judgement for this query category. Average Prevision = none \n")
	   

	if countQuery>0:
		MAP = (MAP*1.0)/countQuery
		MapAt5 = (MapAt5*1.0)/countQuery
		MapAt1 = (MapAt1*1.0)/countQuery
		MapAt3 = (MapAt3*1.0)/countQuery

	f.write("Final MAP\n"+str(MapAt1)+"\t"+str(MapAt3)+"\t"+str(MapAt5)+"\t"+str(MAP)+"\n")
	print("Final MAP\n"+str(MapAt1)+"\t"+str(MapAt3)+"\t"+str(MapAt5)+"\t"+str(MAP)+"\n")
	'''
	f.write("\n"+"Total query with relevance judgement = "+str(countQuery))
	
	f.write("\n"+"Total Mean Average Prevision@1 = "+str(MapAt1)+"\n")
	print("\n"+"Total Mean Average Prevision@1 = "+str(MapAt1)+"\n")
	f.write("\n"+"Total Mean Average Prevision@5 = "+str(MapAt5)+"\n")
	print("\n"+"Total Mean Average Prevision@5 = "+str(MapAt5)+"\n")
	f.write("\n"+"Total Mean Average Prevision@10 = "+str(MAP)+"\n")
	print("\n"+"Total Mean Average Prevision@10 = "+str(MAP)+"\n")
	f.close()
	'''
	#print 'End evaluation at ',datetime.datetime.now()
	# 4. (optional) save a Matlab .mat file
	#io.savemat(save_file_mat,mdict={'X': X, 'BOW_X': BOW_X, 'y': y, 'C': C, 'words': words})
	print 'Done with searching ',datetime.datetime.now(),' Please check the results.'
if __name__ == "__main__":
	main()																							 
