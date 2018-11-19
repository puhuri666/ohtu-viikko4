package ohtu.verkkokauppa;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;

public class VerkkokauppaTest {
    
    Pankki pankki;
    Viitegeneraattori viite;
    Varasto varasto;
    Kauppa k;
    
    @Before
    public void setUp() {
      pankki = mock(Pankki.class);
      viite = mock(Viitegeneraattori.class);
      varasto = mock(Varasto.class);
      k = new Kauppa(varasto, pankki, viite);
    }
    
    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(),anyInt());   
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    @Test
    public void tarkastetaanParametrienArvojenOikeellisuus() {
        when(viite.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 5);
    }
    
    
    @Test
    public void eriTuotteillaOikeaAsiakasTilinumeroJaSumma() {
        when(viite.uusi()).thenReturn(50);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "voi", 15));
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("petteri", "666-666");
        
        verify(pankki).tilisiirto(eq("petteri"), anyInt(), eq("666-666"), anyString(), eq(20));
    }
    
    @Test
    public void samoillaTuotteillaOikeaAsiakasTilinumeroJaSumma() {
        when(viite.uusi()).thenReturn(50);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(1);
        k.tilimaksu("petteri", "666-666");
        verify(pankki).tilisiirto(eq("petteri"), anyInt(), eq("666-666"), anyString(), eq(10));
    }
    
    @Test
    public void toinenTuoteLoppu() {
        when(viite.uusi()).thenReturn(50);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "voi", 15));
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("petteri", "666-666");
        verify(pankki).tilisiirto(eq("petteri"), anyInt(), eq("666-666"), anyString(), eq(5));    
    }
    
    @Test
    public void aloitaAsiointiAlustaaOstoskorin() {
        when(viite.uusi()).thenReturn(50);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "voi", 15));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("petteri", "666-666");
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(15));  
    }
    
    @Test
    public void uusiViiteNumeroJokaiselleMaksutapahtumalle() {
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "voi", 15));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("petteri", "666-666");
        verify(viite, times(1)).uusi();
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("petteri", "666-666");
        verify(viite, times(2)).uusi();
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("petteri", "666-666");
        verify(viite, times(3)).uusi();
    }
    
    @Test
    public void poistaKoristaPoistaaTuotteen() {
       when(varasto.saldo(1)).thenReturn(10);
       when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "voi", 15));
       when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "maito", 5));
       k.aloitaAsiointi();
       k.lisaaKoriin(1);
       k.lisaaKoriin(2);
       k.poistaKorista(2);
       k.tilimaksu("petteri", "666-666");
       verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(15));
    }
    
}
