package de.protubero.beanstore.plugins.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.iterators.SingletonIterator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchEngine {

	public static final Logger log = LoggerFactory.getLogger(SearchEngine.class);

	protected static final String[] FIELDS = { "type", "title", "content" };

	private Directory directory;
	private IndexWriter indexWriter;
	private StandardAnalyzer analyzer;

	public SearchEngine() {
		directory = new ByteBuffersDirectory();
		analyzer = new StandardAnalyzer();

		// init index
		try {
			indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
			indexWriter.close();
		} catch (IOException e) {
			throw new SearchEngineException("error creating initial index writer", e);
		}

		try {
			indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
		} catch (IOException e) {
			throw new SearchEngineException("error creating index writer", e);
		}

	}

	public List<SearchResult> query(String queryString) {
		MultiFieldQueryParser parser = new MultiFieldQueryParser(FIELDS, analyzer);
//		QueryBuilder bldr = new QueryBuilder(analyzer);
//		BooleanQuery.Builder chainQryBldr = new BooleanQuery.Builder();
//		chainQryBldr.add(q1, Occur.SHOULD);		
		
		
		Query query;
		try {
			query = parser.parse(queryString);
		} catch (ParseException e1) {
			throw new SearchEngineException(e1);
		}
		
		try (IndexReader reader = DirectoryReader.open(directory)) {
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs topDocs = searcher.search(query, 10);

			List<SearchResult> documents = new ArrayList<>();

			StoredFields storedFields = searcher.storedFields();
			for (ScoreDoc hit : topDocs.scoreDocs) {
				Document doc = storedFields.document(hit.doc);
				SearchResult listElt = new SearchResult(doc.get("id"), doc.get("type"));
				documents.add(listElt);
			}

			return documents;
		} catch (IOException e) {
			throw new SearchEngineException(e);
		} 
	}

	private void index(Iterator<SearchEngineAction> actionIterator) {

		while (actionIterator.hasNext()) {
			SearchEngineAction action = actionIterator.next();

			if (action.getActionType() == SearchEngineAction.Type.DELETE ||
					action.getActionType() == SearchEngineAction.Type.UPDATE) {
				try {
					indexWriter.deleteDocuments(LongField.newExactQuery("id", action.getId()));
				} catch (IOException e) {
					log.error("error removing document from index " + action.getId(), e);
				}
				break;
			} 
			
			if (action.getActionType() == SearchEngineAction.Type.CREATE ||
					action.getActionType() == SearchEngineAction.Type.UPDATE) {
				// create lucene document
				Document document = new Document();

				document.add(new LongField("id", action.getId(), Field.Store.YES));
				document.add(new StringField("type", action.getType(), Field.Store.YES));
				document.add(new TextField("content", Objects.requireNonNull(action.getContent()), Field.Store.NO));
				
				try {
					indexWriter.addDocument(document);
				} catch (IOException e) {
					log.error("error adding document to the index " + action.getId(), e);
				}
			}
		}

		try {
			indexWriter.flush();
			indexWriter.commit();
		} catch (IOException e) {
			throw new SearchEngineException(e);
		}
	}

	public void index(SearchEngineAction action) {
		index(new SingletonIterator<>(action));
	}

	public void close() {
		try {
			indexWriter.close();
			directory.close();
		} catch (IOException e) {
			log.error("error closing lucene directory", e);
		}
	}


}
