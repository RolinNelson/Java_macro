package macro;

import star.common.*;
import star.base.neo.*;
import star.meshing.*;

import javax.swing.*;
import java.io.File;

@SuppressWarnings("unused")
public class Propeller_02_Import extends StarMacro {

  private static final String[] propsurf_old = { "0", "1", "2" };
  private static final String[] propsurf_new = { "Hub", "Blade-Key", "Blade"};

  public void execute() {
    try{
      //ImportPart(0.0, 1.0);
      CustomImport("P4119.dbs", 0.0, 0.1);
      propeRename("P4119");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, e.toString());
    }
  }
  @SuppressWarnings("IfCanBeSwitch")
  private void CustomImport(String partName, double trans, double scale) {
    Simulation mySim = getActiveSimulation();
    Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }));
    LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
    PartImportManager partImportManager_0 = mySim.get(PartImportManager.class);
    String path = resolveWorkPath();
    String[] strArray = partName.split("\\.");
    int suffixIndex = strArray.length - 1;
    if (strArray[suffixIndex].equals("stl")) {
      String caselsh = partName.substring(0, partName.lastIndexOf("."));
      partImportManager_0.importStlPart(resolvePath(path + File.separator + partName), "OneSurfacePerPatch", units_m, true, 1.0E-5);
      MeshPart meshPart_0 = ((MeshPart) mySim.get(SimulationPartManager.class).getPart(caselsh));
      mySim.get(SimulationPartManager.class).translateParts(new NeoObjectVector(new Object[]{meshPart_0}), new DoubleVector(new double[]{0.0, 0.0, -trans}), new NeoObjectVector(new Object[]{units_m, units_m, units_m}), GCS);
      mySim.get(SimulationPartManager.class).scaleParts(new NeoObjectVector(new Object[]{meshPart_0}), new DoubleVector(new double[]{scale, scale, scale}), GCS);
    } else if (strArray[suffixIndex].equals("dbs")) {
      String caselsh = partName.substring(0, partName.lastIndexOf("."));
      partImportManager_0.importDbsPart(resolvePath(path + File.separator + partName), "OneSurfacePerPatch", "OnePartPerFile", true, units_m, 1);
      MeshPart meshPart_0 = ((MeshPart) mySim.get(SimulationPartManager.class).getPart(caselsh));
      mySim.get(SimulationPartManager.class).translateParts(new NeoObjectVector(new Object[]{meshPart_0}), new DoubleVector(new double[]{0.0, 0.0, -trans}), new NeoObjectVector(new Object[]{units_m, units_m, units_m}), GCS);
      mySim.get(SimulationPartManager.class).scaleParts(new NeoObjectVector(new Object[]{meshPart_0}), new DoubleVector(new double[]{scale, scale, scale}), GCS);
    } else if (strArray[suffixIndex].equals("igs")) {
      String caselsh = partName.substring(0, partName.lastIndexOf("."));
      partImportManager_0.importCadPart(resolvePath(path + File.separator + partName), "SharpEdges", 30.0, 2, 0.001, false, true, 1.0E-5,
          false, false, false, false);
      CadPart cadPart_0 = ((CadPart) mySim.get(SimulationPartManager.class).getPart(caselsh));
      mySim.get(SimulationPartManager.class).translateParts(new NeoObjectVector(new Object[]{cadPart_0}), new DoubleVector(new double[]{0.0, 0.0, -trans}), new NeoObjectVector(new Object[]{units_m, units_m, units_m}), GCS);
      mySim.get(SimulationPartManager.class).scaleParts(new NeoObjectVector(new Object[]{cadPart_0}), new DoubleVector(new double[]{scale, scale, scale}), GCS);
    }
  }

  private void ImportPart(double trans, double scale) {
    String path = resolveWorkPath();
    File file = new File(path);
    File[] fa = file.listFiles();
    if (fa != null) {
      for (File fs : fa) {
        String filename = fs.getName();
        CustomImport(filename, trans, scale);
      }
    }
  }
  @SuppressWarnings("SameParameterValue")
  private void propeRename(String partName) {
    Simulation mySim = getActiveSimulation();
    MeshPart meshPart_0 = ((MeshPart) mySim.get(SimulationPartManager.class).getPart(partName));
    meshPart_0.setPresentationName("Propeller");
    for (int k = 0; k < propsurf_old.length; k++) {
      PartSurface partSurface_0 = meshPart_0.getPartSurfaceManager().getPartSurface(propsurf_old[k]);
      partSurface_0.setPresentationName(propsurf_new[k]);
    }
    PartCurve partCurve_0 = meshPart_0.getPartCurveManager().getPartCurve("0");
    partCurve_0.setPresentationName("Blade Edges");
  }

}