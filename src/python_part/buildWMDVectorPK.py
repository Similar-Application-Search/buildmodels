# 	modified by Chengjun Yuan <cy3yb@virginia.edu>
#	April 11th 2016
#	input: 	word2vec.model  
#			docForWmd.txt
#	output:	distanceMatrix.csv
#	Usage: python querySimilarProjectsInWmd.py word2vec.model docForWmd.txt projectDetails.txt queryProjectName rankedSimilarProjects.csv

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


def main():
	global X, BOW_X, queryIndex
	if len(sys.argv) != 4:
		print "Usage: python buildWMDVectorPK.py ./word2vec_des_java.model.bin ./eval_data/trainDocForWMD.txt ./vectorsPK/trainDocVectorPK.pk >output_wmd_pk_java_projects_train &"
		sys.exit()
	#print 'Start Loading word2vec.model',datetime.datetime.now()	
	# 0. specify train/test datasets
	word2vec_model = sys.argv[1] # e.g.: 'GoogleNews-vectors-negative300.bin'
	dataset = sys.argv[2] # e.g.: 'twitter.txt'
	vectorName = sys.argv[3]

	if word2vec_model.endswith('.bin') :
		model = gensim.models.Word2Vec.load_word2vec_format(word2vec_model, binary=True)
	else :
		model = gensim.models.Word2Vec.load(word2vec_model)
	#print 'End Loading word2vec.model',datetime.datetime.now()	
	vec_size = 300
	
	# 2. read document data

	#print 'Start Loading training data .txt',datetime.datetime.now()
	X, BOW_X, y, C, words, wmdWords= read_line_by_line(dataset,[],model,vec_size)
	#print 'End Loading training data .txt',datetime.datetime.now()

	# 3. save pickle of extracted variables
	
	#print 'Start dumping train_vectors.pk',datetime.datetime.now()
	with open(vectorName, 'w') as f:
		pickle.dump([X, BOW_X, y, C, words], f)
	#print 'End dumping train_vectors.pk',datetime.datetime.now()

	
	print 'Finish all at ',datetime.datetime.now(), ' for = ',vectorName, ' Please proceed to next step'
if __name__ == "__main__":
	main()																							 
