package wethinkcode.places.db.memory;

import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * TODO: javadoc PlacesDb
 */
public class PlacesDb implements Places
{
    private Collection<String> validProvinces = new ArrayList<>();
    {
        validProvinces.add("MPUMALANGA");
        validProvinces.add("KWAZULU-NATAL");
        validProvinces.add("GAUTENG");
        validProvinces.add("LIMPOPO");
        validProvinces.add("FREE STATE");
        validProvinces.add("NORTH WEST");
        validProvinces.add("WESTERN CAPE");
        validProvinces.add("EASTERN CAPE");
        validProvinces.add("NORTHERN CAPE");
    };
    private final Set<Town> towns = new TreeSet<>();
    private Collection<String> provinces;

    public PlacesDb( Set<Town> places ){
        towns.addAll( places );
        setProvinces();
    }

    public PlacesDb() {
    }

    private void setProvinces() {
        this.provinces = new ArrayList<>();
        for (Town town : towns) {
            if (!provinces.contains(town.getProvince().toUpperCase()) && validProvinces.contains(town.getProvince().toUpperCase())) {
                provinces.add(town.getProvince().toUpperCase());
            }
        }
    }

    @Override
    public Collection<String> provinces(){
        return this.provinces;
    }

    @Override
    public Collection<Town> townsIn( String aProvince ){
        Collection<Town> resultTowns = new ArrayList<>();
        for (Town aTown : towns) {
            if (aTown.getProvince().toUpperCase().equals(aProvince)) {
                resultTowns.add(aTown);
            }
        }
        return resultTowns;
    }

    @Override
    public int size(){
        return towns.size();
    }

    @Override
    public void add(Town town) {
        this.towns.add(town);
        
    }

    @Override
    public Town get(int index) {
        try {
            return (Town) this.towns.toArray()[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    

}