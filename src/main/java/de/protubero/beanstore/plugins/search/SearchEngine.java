package de.protubero.beanstore.plugins.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.collections4.iterators.SingletonIterator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class SearchEngine {

	public static final Logger log = LoggerFactory.getLogger(SearchEngine.class);

	protected static final String[] FIELDS = {"type", "title", "content"};

	private Consumer<Iterator<SearchEngineAction>> indexer;
	private Function<String, List<SearchResult>> queryFunction;
	private Runnable closer;

	public SearchEngine() {
		Directory directory = new RAMDirectory();
		StandardAnalyzer analyzer = new StandardAnalyzer();

		// init index
		IndexWriter tempIndexWriter;
		try {
			tempIndexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
		} catch (IOException e3) {
			throw new SearchEngineException("error creating initial index writer", e3);
		}
		try {
			tempIndexWriter.close();
		} catch (IOException e2) {
			throw new SearchEngineException("error closing initial index writer", e2);
		}
		
		indexer = new Consumer<Iterator<SearchEngineAction>>() {

			@Override
			public void accept(Iterator<SearchEngineAction> actionIterator) {
				try (IndexWriter tempIndexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {

					while (actionIterator.hasNext()) {
						SearchEngineAction action = actionIterator.next();
						Term term = new Term("id", action.getId());
						
						if (action.getActionType() == SearchEngineAction.Type.DELETE) {
							try {
								tempIndexWriter.deleteDocuments(term);
							} catch (IOException e) {
								log.error("error removing document from index " + action.getId(), e);
							}
							break;
						} else {	
							
							// create lucene document
							Document document = new Document();

							document.add(new StringField("id", action.getId(), Field.Store.YES));
							document.add(new StringField("type", action.getType(), Field.Store.YES));
							document.add(new TextField("content", Objects.requireNonNull(action.getContent()), Field.Store.NO));
							
							switch (action.getActionType()) {
							case CREATE:
								try {
									tempIndexWriter.addDocument(document);
								} catch (IOException e) {
									log.error("error adding document to the index " + action.getId(), e);
								}
								break;
							case UPDATE:
								try {
									tempIndexWriter.updateDocument(term, document);
								} catch (IOException e) {
									log.error("error updating document at the index " + action.getId(), e);
								}
								break;
							default:
								throw new AssertionError();	
							}
						}
						
					}
				} catch (IOException e1) {
					log.error("error closing index writer", e1);
				}

			}

		};

		queryFunction = new Function<String, List<SearchResult>>() {

			@Override
			public List<SearchResult> apply(String queryString) {
				MultiFieldQueryParser parser = new MultiFieldQueryParser(FIELDS, analyzer);

				Query query;
				try {
					query = parser.parse(queryString);
				} catch (ParseException e1) {
					throw new SearchEngineException(e1);
				}

				TopDocs topDocs;
				IndexReader reader = null;
				try {
					reader = DirectoryReader.open(directory);
					IndexSearcher searcher = new IndexSearcher(reader);
					topDocs = searcher.search(query, 10);

					List<SearchResult> documents = new ArrayList<>();
					for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
						Document doc = searcher.doc(scoreDoc.doc);
						SearchResult listElt = new SearchResult(doc.get("id"), doc.get("type"));
						documents.add(listElt);
					}

					return documents;
				} catch (IOException e) {
					throw new SearchEngineException(e);
				} finally {
					try {
						if (reader != null) {
							reader.close();
						}
					} catch (IOException e) {
						log.error("error closing the reader", e);
					}
				}
			}
		};

		closer = () -> {
			try {
				directory.close();
			} catch (IOException e) {
				log.error("error closing lucene directory", e);
			}
		};
		
	}
	
	public void index(SearchEngineAction action) {
		index(new SingletonIterator<>(action));
	}

	public void index(Iterator<SearchEngineAction> actionIterator) {
		indexer.accept(actionIterator);
	}
	
	public List<SearchResult> query(String queryStr) {
		return queryFunction.apply(queryStr);
	}

	public void close() {
		closer.run();
	}
	

	
}
