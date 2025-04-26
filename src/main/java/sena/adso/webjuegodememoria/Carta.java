package sena.adso.webjuegodememoria;

public class Carta {
    private String id;
    private boolean revelada;
    private boolean emparejada;

    public Carta(String id) {
        this.id = id;
        this.revelada = false;
        this.emparejada = false;        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRevelada() {
        return revelada;
    }

    public void setRevelada(boolean revelada) {
        this.revelada = revelada;
    }

    public boolean isEmparejada() {
        return emparejada;
    }

    public void setEmparejada(boolean emparejada) {
        this.emparejada = emparejada;
    }
    
    
    
    
}
