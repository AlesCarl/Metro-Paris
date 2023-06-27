package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge> grafo ;
	private List<Fermata> fermate ;
	private Map<Integer, Fermata> fermateIdMap ;
	
	public void creaGrafo() {
		
		// crea l'oggetto grafo
		this.grafo = new SimpleGraph<Fermata, DefaultEdge>(DefaultEdge.class) ;
		
		// aggiungi i vertici
		MetroDAO dao = new MetroDAO() ;
		this.fermate = dao.readFermate() ;
		
		
		fermateIdMap = new HashMap<>();
		for(Fermata f: this.fermate)
			this.fermateIdMap.put(f.getIdFermata(), f) ;
		
		
		Graphs.addAllVertices(this.grafo, this.fermate) ;
		
		// aggiungi gli archi
		
		// metodo 1: considero tutti i potenziali archi
//		long tic = System.currentTimeMillis();
//		for(Fermata partenza: this.grafo.vertexSet()) {
//			for(Fermata arrivo: this.grafo.vertexSet()) {
//				if(dao.isConnesse(partenza, arrivo)) {
//					this.grafo.addEdge(partenza, arrivo) ;
//				}
//			}
//		}
		
		// metodo 2: data una fermata, trova la lista di quelle adiacente
		long tic = System.currentTimeMillis();
		for(Fermata partenza: this.grafo.vertexSet()) {
			List<Fermata> collegate = dao.trovaCollegate(partenza) ;
			
			for(Fermata arrivo: collegate) {
				this.grafo.addEdge(partenza, arrivo) ;
			}
		}
		long toc = System.currentTimeMillis();
		System.out.println("Elapsed time "+ (toc-tic));
		
		
		
		
		// metodo 2a: data una fermata, troviamo la lista di id connessi
		tic = System.currentTimeMillis();
		for(Fermata partenza: this.grafo.vertexSet()) {
			List<Fermata> collegate = dao.trovaIdCollegate(partenza, fermateIdMap) ;
			
			for(Fermata arrivo: collegate) {
				this.grafo.addEdge(partenza, arrivo) ;
			}
		}
		toc = System.currentTimeMillis();
		System.out.println("Elapsed time "+ (toc-tic));
		
		
		
		
		
		
		// metodo 3: faccio una query per prendermi tutti gli edges 
		
		tic = System.currentTimeMillis();
		List<coppieF> allCoppie = dao.getAllCoppie(fermateIdMap);
		for (coppieF coppia : allCoppie)
			this.grafo.addEdge(coppia.getPartenza(), coppia.getArrivo());
		
		toc = System.currentTimeMillis();
		System.out.println("Elapsed time "+ (toc-tic));
		
		
		
		
		System.out.println("Grafo creato con "+this.grafo.vertexSet().size() +
				" vertici e " + this.grafo.edgeSet().size() + " archi") ;
		System.out.println(this.grafo);
	}
	
	
	
	
	
/** determina il percorso minimo (visita in ampiezza)  tra le 2 fermate, GRAFO NON PESATO */
	
	public List<Fermata> percorso(Fermata partenza, Fermata arrivo) {
		
		// Visita il grafo partendo da 'partenza'
		BreadthFirstIterator<Fermata, DefaultEdge> visita = 
				new BreadthFirstIterator<>(this.grafo, partenza) ;
	/* visita tutti i nodi a distanza 1 dalla sorgente, poi tutti i nodi a distanza 2, e così via, fino a quando tutti
	 * i nodi accessibili dal punto di partenza non sono stati visitati. */
	 
		
		
		/* SOLO A SCOPO DIMOSTRATIVO : (utile per trovare il grado di un vertice) 
		 
		List<Fermata> raggiungibili = new ArrayList<Fermata>() ;
		
		while(visita.hasNext()) {
			Fermata f = visita.next() ;
			raggiungibili.add(f) ;
		}
		System.out.println(raggiungibili) ;
		
		*/ 
		
		
		// Trova il percorso sull'albero di visita: 
		List<Fermata> percorso = new ArrayList<Fermata>() ;
		Fermata corrente = arrivo ;
		percorso.add(arrivo) ;
		
		// "arco con cui ci sono arrivato": 
		DefaultEdge e = visita.getSpanningTreeEdge(corrente) ; // ** serve a ricostruire il percorso *** 
		
		
		while(e!=null) {  // "null" significa che siamo arrivati alla fine 
			
			// "il precedente è l'opposto al vertice ... 
			Fermata precedente = Graphs.getOppositeVertex(this.grafo, e, corrente) ;
			
			percorso.add(0, precedente) ;
			corrente = precedente ;
			
			e = visita.getSpanningTreeEdge(corrente) ;
		}
		
		return percorso ;
	}
	
	
	
	/* determina il percorso minimo tra le 2 fermate ( si usa un grafo pesto) */
	/** 
	public List<Fermata> percorso(Fermata partenza, Fermata arrivo) {

		DijkstraShortestPath<Fermata, DefaultWeightedEdge> sp = 
				new DijkstraShortestPath<>(this.grafo) ;
		
		GraphPath<Fermata, DefaultWeightedEdge> gp = sp.getPath(partenza, arrivo) ;
		
		return gp.getVertexList() ;
	}
	
	*/ 
	
	
	public List<Fermata> getAllFermate(){
		MetroDAO dao = new MetroDAO() ;
		return dao.readFermate() ;
	}
	
	public boolean isGrafoLoaded() {
		return this.grafo.vertexSet().size()>0;
	}

}











