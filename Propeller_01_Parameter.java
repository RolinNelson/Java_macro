package macro;

import star.common.*;
import star.base.neo.*;

import javax.swing.*;

@SuppressWarnings("unused")
public class Propeller_01_Parameter extends StarMacro {

  private static final String[] Scalar_ID = { "Dp", "n", "J", "Pnp", "Psp", "Ptp", "GD" };

  private static final double[] Scalar_param = { 0.304, 10.0, 0.5, 5.0, 1.2, 0.02, 50.0 };

  public void execute() {

    Simulation mySim = getActiveSimulation();
    try{
      for ( int i = 0; i < Scalar_ID.length; i++ ) {
        createParameter(Scalar_ID[i], Scalar_param[i]);
      }
      mySim.println("Note: Created " + Scalar_ID.length + " parameters");
      createCoordinate(new double[]{0.0, 0.0, 0.0}, new double[]{0.0, 0.0, 0.0});

    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, e.toString());
    }
  }
  private void createParameter(String name, double param) {
    Simulation mySim = getActiveSimulation();
    mySim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
    ScalarGlobalParameter Parameter = ((ScalarGlobalParameter) mySim.get(GlobalParameterManager.class).getObject("Scalar"));
    Parameter.setPresentationName(name);
    Parameter.getQuantity().setValue(param);
    //Parameter.getQuantity().setDefinition(String.valueOf(param));
  }
  @SuppressWarnings({"SameParameterValue", "DuplicatedCode"})
  private void createCoordinate(double[] param1, double[] param2) {
    Simulation mySim = getActiveSimulation();
    Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
    CartesianCoordinateSystem LCS_H = GCS.getLocalCoordinateSystemManager().createLocalCoordinateSystem(CartesianCoordinateSystem.class, "Cartesian");
    LCS_H.setBasis0(new DoubleVector(new double[] {1.0, 0.0, 0.0}));
    LCS_H.setBasis1(new DoubleVector(new double[] {0.0, 1.0, 0.0}));
    LCS_H.setPresentationName("Initial COS");
    CartesianCoordinateSystem LCS_HP = LCS_H.getLocalCoordinateSystemManager().createLocalCoordinateSystem(CartesianCoordinateSystem.class, "Cartesian");
    LCS_HP.setBasis0(new DoubleVector(new double[] {1.0, 0.0, 0.0}));
    LCS_HP.setBasis1(new DoubleVector(new double[] {0.0, 1.0, 0.0}));
    LCS_HP.setPresentationName("Propeller");
    try{
      LCS_H.getOrigin().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[] {param1[0], param1[1], param1[2]}));
      LCS_HP.getOrigin().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[] {param2[0]-param1[0], param2[1]-param1[1], param2[2]-param1[2]}));
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Note: Please set the position of the ship coordinate system");
    }
  }
  private void createCoordinate() {
    Simulation mySim = getActiveSimulation();
    // Propeller Coordinate
    Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
    CartesianCoordinateSystem LCS_P = GCS.getLocalCoordinateSystemManager().createLocalCoordinateSystem(CartesianCoordinateSystem.class, "Cartesian");
    LCS_P.getOrigin().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[] {0, 0.0, 0.0}));
    LCS_P.setBasis0(new DoubleVector(new double[] {1.0, 0.0, 0.0}));
    LCS_P.setBasis1(new DoubleVector(new double[] {0.0, 1.0, 0.0}));
    LCS_P.setPresentationName("Propeller");
  }
}