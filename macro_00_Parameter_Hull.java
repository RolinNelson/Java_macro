package macro;

import star.base.neo.*;
import star.common.*;

import javax.swing.*;

@SuppressWarnings("unused")
public class macro_00_Parameter_Hull extends StarMacro {

  private static final String[] Scalar_ID = { "Cds", "Lm", "Bm", "Dm", "Tm", "Fst", "Pn", "Ps", "Pt", "Vm", "TC", "Mass", "rho", "mu" };
  private static final Double[] Scalar_param = { 150.0, 154.8, 35.0, 16.9, 7.5, 0.65, 12.0, 1.5, 0.7, 6.1728, 150.0, 1000.0, 1026.0210, 0.001220 };
  private static final double[] GCS_Ship = {72.8, 0.0, 4.5};
  private static final double[] GCS_Prop = {0.0, 0.0, -4.5};

  public void execute() {
    Simulation mySim = getActiveSimulation();

    try {
      for ( int i = 0; i < Scalar_ID.length; i++ ) {
        createParameter(Scalar_ID[i], Scalar_param[i]);
      }
      mySim.println("Note: Created " + Scalar_ID.length + " parameters");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, e.toString());
    }
    createCoordinate(GCS_Ship, GCS_Prop);
  }
  private void createParameter(String name, double param) {
    Simulation mySim = getActiveSimulation();
    mySim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
    ScalarGlobalParameter Parameter = ((ScalarGlobalParameter) mySim.get(GlobalParameterManager.class).getObject("Scalar"));
    Parameter.setPresentationName(name);
    Parameter.getQuantity().setValue(param);
    //Parameter.getQuantity().setDefinition(String.valueOf(param));
  }
  private void createFunction(String name, String param) {
    Simulation mySim = getActiveSimulation();
    UserFieldFunction Function = mySim.getFieldFunctionManager().createFieldFunction();
    Function.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
    Function.setPresentationName(name);
    Function.setFunctionName(name);
    Function.setDefinition(param);
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
}