package macro;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.flow.*;
import star.keturb.*;
import star.kwturb.*;
import star.material.*;
import star.meshing.*;
import star.metrics.*;
import star.mixturemultiphase.*;
import star.motion.*;
import star.multiphase.*;
import star.prismmesher.*;
import star.sixdof.*;
import star.trimmer.*;
import star.turbulence.*;
import star.vis.*;
import star.vof.*;
import star.walldistance.*;

@SuppressWarnings({"unchecked", "unused", "ResultOfMethodCallIgnored"})
public class macro_02_BasicSetting_Hull extends StarMacro {

  static boolean Overset, Propeller, DES;

  static String[] ParamName = { "Vs", "Tm", "Vm", "Fst" };
  static Double[] Param_Vs = { 8.0, 10.0, 11.0, 11.5, 12.0, 12.5, 13.0, 14.0 };
  static Double[] Param_Tm = { 7.5, 8.0 };
  static Double[] Param_Vm = { 4.1152, 5.1440, 5.6584, 5.9156, 6.1728, 6.4300, 6.6872, 7.2016 };
  static Double[] Param_Fst = { 0.29, 0.45, 0.54, 0.59, 0.65, 0.7, 0.76, 0.88 };

  public void execute() {
    SetSwitch(false, true, false);
    test(false);
    play(new StaticDomain());
    if (Overset) {
      play(new OversetDomain());
      if (Propeller) {
        play(new RotationDomain());
      }
    }
    play(new GeometryScene());
    play(new PhysicsField());
    play(new BoundaryCondition());
    play(new MeshContinuum());
    play(new GenerateMesh());

//    play(new CreateReport());
//    play(new CreateScene());
//    play(new WaveCut());
//    play(new SetupSolver());
//    play(new SaveFile());

    play(new CircularSave());
  }

  public void test(boolean run) {
    if ( run ) {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is test program");
      try{
        mySim.println("@Try running test program");


      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: Test completed");
    }
  }

  public static void SetSwitch(boolean overset, boolean propeller, boolean des) {
    Overset = overset;
    Propeller = propeller;
    DES = des;
  }

  public static class StaticDomain extends StarMacro {
    @Override
    public void execute() {
      // Create < Far Field >
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create static domain program");
      try {
        mySim.println("@Try running create static domain program");
        LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
        MeshPartFactory meshPartFactory_0 = mySim.get(MeshPartFactory.class);
        SimpleBlockPart Part_FarField = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
        Part_FarField.setCoordinateSystem(GCS);
        Part_FarField.getCorner1().setCoordinateSystem(GCS);
        Part_FarField.getCorner1().setDefinition("[-2*${Cds}, 0.0, -2*${Cds}]");
        Part_FarField.getCorner2().setCoordinateSystem(GCS);
        Part_FarField.getCorner2().setDefinition("[2*${Cds}, 2*${Cds}, ${Cds}]");
        Part_FarField.rebuildSimpleShapePart();
        Part_FarField.setPresentationName("Far Field");
        PartSurface FarField_Out = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface");
        Part_FarField.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(new Object[]{FarField_Out}), 89.0);
        FarField_Out.setPresentationName("Outlet");
        PartSurface FarField_In = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 6");
        FarField_In.setPresentationName("Inlet");
        PartSurface FarField_Top = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 4");
        FarField_Top.setPresentationName("Top");
        PartSurface FarField_Bot = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 3");
        FarField_Bot.setPresentationName("Bottom");
        PartSurface FarField_Port = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 5");
        FarField_Port.setPresentationName("PortSide");
        PartSurface FarField_Sym = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 2");
        FarField_Sym.setPresentationName("Symmetry");
        // Create < 01-Static Domain >
        MeshPart Part_Ship = ((MeshPart) mySim.get(SimulationPartManager.class).getPart("Ship"));
        //meshPart_0.setPresentationName("Ship");
        SubtractPartsOperation Operation_StaticDomain = (SubtractPartsOperation) mySim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{Part_FarField, Part_Ship}));
        Operation_StaticDomain.getTargetPartManager().setObjects(Part_FarField);
        Operation_StaticDomain.setPerformCADBoolean(false);
        Operation_StaticDomain.execute();
        Operation_StaticDomain.setPresentationName("01-Static Domain");
        MeshOperationPart Part_StaticDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Subtract"));
        Part_StaticDomain.setPresentationName("01-Static Domain");
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The static domain creation completed");
    }
  }

  public static class OversetDomain extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      mySim.println("Note: This is create overset domain program");
      try {
        mySim.println("@Try running create overset domain program");
        // Create < Overlap Field >
        MeshPartFactory meshPartFactory_0 = mySim.get(MeshPartFactory.class);
        SimpleBlockPart Part_OverlapField = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
        Part_OverlapField.setCoordinateSystem(GCS);
        Part_OverlapField.getCorner1().setCoordinateSystem(GCS);
        Part_OverlapField.getCorner1().setDefinition("[-0.12*${Lm}, -1.0*${Bm}, -1.5*${Dm}]");
        Part_OverlapField.getCorner2().setCoordinateSystem(GCS);
        Part_OverlapField.getCorner2().setDefinition("[1.12*${Lm}, 1.0*${Bm}, 1.5*${Dm}]");
        Part_OverlapField.rebuildSimpleShapePart();
        Part_OverlapField.setPresentationName("Overlap Field");
        PartSurface OverlapField_Overset = Part_OverlapField.getPartSurfaceManager().getPartSurface("Block Surface");
        Part_OverlapField.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(new Object[]{OverlapField_Overset}), 89.0);
        PartSurface OverlapField_Sym = Part_OverlapField.getPartSurfaceManager().getPartSurface("Block Surface 2");
        OverlapField_Sym.setPresentationName("Symmetry");
        PartSurface OverlapField_1 = Part_OverlapField.getPartSurfaceManager().getPartSurface("Block Surface 3");
        PartSurface OverlapField_2 = Part_OverlapField.getPartSurfaceManager().getPartSurface("Block Surface 4");
        PartSurface OverlapField_3 = Part_OverlapField.getPartSurfaceManager().getPartSurface("Block Surface 5");
        PartSurface OverlapField_4 = Part_OverlapField.getPartSurfaceManager().getPartSurface("Block Surface 6");
        Part_OverlapField.combinePartSurfaces(new NeoObjectVector(new Object[]{OverlapField_Overset, OverlapField_1, OverlapField_2, OverlapField_3, OverlapField_4}));
        OverlapField_Overset.setPresentationName("Overset");
        if (Propeller) {
          Part_OverlapField.combinePartSurfaces(new NeoObjectVector(new Object[]{OverlapField_Overset, OverlapField_Sym}));
        }
        // Create < 02-Overset Domain >
        MeshPart Part_Ship = ((MeshPart) mySim.get(SimulationPartManager.class).getPart("Ship"));
        SubtractPartsOperation Operation_OversetDomain = (SubtractPartsOperation) mySim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{Part_OverlapField, Part_Ship}));
        Operation_OversetDomain.getTargetPartManager().setObjects(Part_OverlapField);
        Operation_OversetDomain.setPerformCADBoolean(false);
        Operation_OversetDomain.execute();
        Operation_OversetDomain.setPresentationName("02-Overset Domain");
        MeshOperationPart Part_OversetDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Subtract"));
        Part_OversetDomain.setPresentationName("02-Overset Domain");
        // Modify < 01-Static Domain >
        SubtractPartsOperation Operation_StaticDomain = ((SubtractPartsOperation) mySim.get(MeshOperationManager.class).getObject("01-Static Domain"));
        SimpleBlockPart Part_FarField = ((SimpleBlockPart) mySim.get(SimulationPartManager.class).getPart("Far Field"));
        Operation_StaticDomain.getInputGeometryObjects().setObjects(Part_FarField);
        Operation_StaticDomain.execute();
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The overset domain creation completed");
    }
  }

  public static class RotationDomain extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      mySim.println("Note: This is create rotation domain program");
      try {
        mySim.println("@Try running create rotation domain program");
        CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
        CartesianCoordinateSystem LCS_P = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
        MeshPart Part_Prop = ((MeshPart) mySim.get(SimulationPartManager.class).getPart("Propeller"));
        // Create < Rotation Field >
        MeshPartFactory meshPartFactory_0 = mySim.get(MeshPartFactory.class);
        SimpleCylinderPart Part_RotationField = meshPartFactory_0.createNewCylinderPart(mySim.get(SimulationPartManager.class));
        Part_RotationField.setCoordinateSystem(LCS_P);
        Part_RotationField.getStartCoordinate().setCoordinateSystem(LCS_P);
        Part_RotationField.getStartCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{3.0, 0.0, 0.0}));
        Part_RotationField.getEndCoordinate().setCoordinateSystem(LCS_P);
        Part_RotationField.getEndCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{-3.0, 0.0, 0.0}));
        Part_RotationField.getRadius().setUnits(units_m);
        Part_RotationField.getRadius().setDefinition("0.6*${Dp}");
        Part_RotationField.rebuildSimpleShapePart();
        Part_RotationField.setPresentationName("Rotation Field");
        PartSurface part_Inter = Part_RotationField.getPartSurfaceManager().getPartSurface("Cylinder Surface");
        part_Inter.setPresentationName("Interface");
        // Modity < 01-Static Domain >
        SimpleBlockPart Part_FarField = ((SimpleBlockPart) mySim.get(SimulationPartManager.class).getPart("Far Field"));
        Part_FarField.getCorner1().setDefinition("[-2*${Cds}, -2*${Cds}, -2*${Cds}]");
        PartSurface FarField_Sym = Part_FarField.getPartSurfaceManager().getPartSurface("Symmetry");
        FarField_Sym.setPresentationName("Starboard");
        SubtractPartsOperation Operation_SDomain = ((SubtractPartsOperation) mySim.get(MeshOperationManager.class).getObject("01-Static Domain"));
        Operation_SDomain.execute();
        // Modity < 02-Overset Domain >
        MeshPart Part_Ship = ((MeshPart) mySim.get(SimulationPartManager.class).getPart("Ship"));
        SimpleBlockPart Part_OverlapField = ((SimpleBlockPart) mySim.get(SimulationPartManager.class).getPart("Overlap Field"));
        SubtractPartsOperation Operation_OversetDomain = ((SubtractPartsOperation) mySim.get(MeshOperationManager.class).getObject("02-Overset Domain"));
        Operation_OversetDomain.getInputGeometryObjects().setObjects(Part_OverlapField, Part_RotationField, Part_Ship);
        Operation_OversetDomain.getTargetPartManager().setObjects(Part_OverlapField);
        Operation_OversetDomain.execute();
        // Create < 03-Rotation Domain >
        SubtractPartsOperation Operation_RotationDomain =
            (SubtractPartsOperation) mySim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{Part_RotationField, Part_Ship, Part_Prop}));
        Operation_RotationDomain.getTargetPartManager().setObjects(Part_RotationField);
        Operation_RotationDomain.setPerformCADBoolean(false);
        Operation_RotationDomain.execute();
        Operation_RotationDomain.setPresentationName("03-Rotation Domain");
        MeshOperationPart Part_RDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Subtract"));
        Part_RDomain.setPresentationName("03-Rotation Domain");
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The rotation domain creation completed");
    }
  }

  public static class GeometryScene extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create geometry scene program");
      try {
        mySim.println("@Try running create geometry scene program");
        MeshOperationPart Part_SDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("01-Static Domain"));
        mySim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{Part_SDomain}), "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, RegionManager.CreateInterfaceMode.NONE);
        if (Overset) {
          MeshOperationPart Part_ODomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("02-Overset Domain"));
          mySim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{Part_ODomain}), "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null, "OneFeatureCurvePerPartCurve", null, RegionManager.CreateInterfaceMode.NONE);
          if (Propeller) {
            MeshOperationPart Part_RDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("03-Rotation Domain"));
            mySim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{Part_RDomain}), "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null, "OneFeatureCurvePerPartCurve", null, RegionManager.CreateInterfaceMode.NONE);
          }
        }
        PartSurface SDomain_In = Part_SDomain.getPartSurfaceManager().getPartSurface("Far Field.Inlet");
        PartSurface SDomain_Out = Part_SDomain.getPartSurfaceManager().getPartSurface("Far Field.Outlet");
        PartSurface SDomain_Top = Part_SDomain.getPartSurfaceManager().getPartSurface("Far Field.Top");
        PartSurface SDomain_Bot = Part_SDomain.getPartSurfaceManager().getPartSurface("Far Field.Bottom");
        PartSurface SDomain_Port = Part_SDomain.getPartSurfaceManager().getPartSurface("Far Field.PortSide");
        // Create < 01-Geometry Scene >
        mySim.getSceneManager().createEmptyScene("Scene");
        Scene scene_1 = mySim.getSceneManager().getScene("Scene 1");
        scene_1.initializeAndWait();
        scene_1.resetCamera();
        scene_1.setPresentationName("01-Geometry");
        PartDisplayer partDisplayer_1 = scene_1.getDisplayerManager().createPartDisplayer("Geometry", -1, 4);
        partDisplayer_1.initialize();
        partDisplayer_1.setOutline(false);
        partDisplayer_1.setSurface(true);
        partDisplayer_1.setColorMode(PartColorMode.DP);
        if (!Overset) {
          PartSurface SDomain_Hull = Part_SDomain.getPartSurfaceManager().getPartSurface("Ship.Hull");
          partDisplayer_1.getInputParts().setObjects(SDomain_Hull, SDomain_In, SDomain_Out, SDomain_Top, SDomain_Bot, SDomain_Port);
        } else if (!Propeller) {
          MeshOperationPart Part_OversetDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("02-Overset Domain"));
          PartSurface OversetDomain_Hull = Part_OversetDomain.getPartSurfaceManager().getPartSurface("Ship.Hull");
          partDisplayer_1.getInputParts().setObjects(OversetDomain_Hull, SDomain_In, SDomain_Out, SDomain_Top, SDomain_Bot, SDomain_Port);
        } else {
          MeshOperationPart Part_ODomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("02-Overset Domain"));
          PartSurface ODomain_Hull = Part_ODomain.getPartSurfaceManager().getPartSurface("Ship.Hull");
          MeshOperationPart Part_RDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("03-Rotation Domain"));
          PartSurface RDomain_BK = Part_RDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade-Key");
          PartSurface RDomain_B = Part_RDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade");
          PartSurface RDomain_H = Part_RDomain.getPartSurfaceManager().getPartSurface("Propeller.Hub");
          partDisplayer_1.getInputParts().setObjects(RDomain_BK, RDomain_B, RDomain_H, ODomain_Hull, SDomain_In, SDomain_Out, SDomain_Top, SDomain_Bot, SDomain_Port);
        }
        scene_1.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
        scene_1.resetCamera();
        scene_1.setTransparencyOverrideMode(SceneTransparencyOverride.MAKE_SCENE_TRANSPARENT);
        CurrentView currentView_1 = scene_1.getCurrentView();
        ViewAngle viewAngle_1 = currentView_1.getViewAngle();
        viewAngle_1.setValue(20.0);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The geometry scene creation completed");
    }
  }

  public static class PhysicsField extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create physics field program");
      try {
        mySim.println("@Try running create physics field program");
        // Create < Physics Field > - K-Epsilon
        PhysicsContinuum PhysicsField = mySim.getContinuumManager().createContinuum(PhysicsContinuum.class);
        PhysicsField.setPresentationName("Physics Field");
        PhysicsField.enable(ThreeDimensionalModel.class);
        PhysicsField.enable(ImplicitUnsteadyModel.class);
        PhysicsField.enable(EulerianMultiPhaseModel.class);
        PhysicsField.enable(SegregatedVofModel.class);
        PhysicsField.enable(SegregatedVolumeFluxBasedFlowModel.class);
        PhysicsField.enable(TurbulentModel.class);
        if (!DES) {
          //RANS
          PhysicsField.enable(RansTurbulenceModel.class);
          PhysicsField.enable(KEpsilonTurbulence.class);
          PhysicsField.enable(RkeTwoLayerTurbModel.class);
          PhysicsField.enable(KeTwoLayerAllYplusWallTreatment.class);
        } else {
          //DES
          PhysicsField.enable(DesTurbulenceModel.class);
          PhysicsField.enable(SstKwTurbDesModel.class);
          PhysicsField.enable(KwAllYplusWallTreatment.class);
          PhysicsField.getReferenceValues().get(MinimumAllowableWallDistance.class).setValue(1.0E-8);
        }
        PhysicsField.enable(GravityModel.class);
        PhysicsField.enable(CellQualityRemediationModel.class);
        PhysicsField.enable(VofWaveModel.class);
        // Euler Phase
        EulerianMultiPhaseModel eulerianMultiPhaseModel_0 = PhysicsField.getModelManager().getModel(EulerianMultiPhaseModel.class);
        EulerianPhase waterPhase = eulerianMultiPhaseModel_0.createPhase();
        waterPhase.setPresentationName("water");
        waterPhase.enable(SinglePhaseLiquidModel.class);
        waterPhase.enable(ConstantDensityModel.class);
        SinglePhaseLiquidModel singlePhaseLiquidModel_0 = waterPhase.getModelManager().getModel(SinglePhaseLiquidModel.class);
        SinglePhaseLiquid singlePhaseLiquid_0 = ((SinglePhaseLiquid) singlePhaseLiquidModel_0.getMaterial());
        ConstantMaterialPropertyMethod constantMaterialPropertyMethod_0 = ((ConstantMaterialPropertyMethod) singlePhaseLiquid_0.getMaterialProperties().getMaterialProperty(ConstantDensityProperty.class).getMethod());
        constantMaterialPropertyMethod_0.getQuantity().setDefinition("${rho}");
        ConstantMaterialPropertyMethod constantMaterialPropertyMethod_1 = ((ConstantMaterialPropertyMethod) singlePhaseLiquid_0.getMaterialProperties().getMaterialProperty(DynamicViscosityProperty.class).getMethod());
        constantMaterialPropertyMethod_1.getQuantity().setDefinition("${mu}");
        EulerianPhase airPhase = eulerianMultiPhaseModel_0.createPhase();
        airPhase.setPresentationName("air");
        airPhase.enable(SinglePhaseGasModel.class);
        airPhase.enable(ConstantDensityModel.class);
        // VOF
        VofWaveModel vofWaveModel_0 = PhysicsField.getModelManager().getModel(VofWaveModel.class);
        FlatVofWave flatVofWave_0 = vofWaveModel_0.getVofWaveManager().createVofWave(FlatVofWave.class, "FlatVofWave");
        flatVofWave_0.setPresentationName("Still water");
        flatVofWave_0.getPointOnLevel().setComponents(0.0, 0.0, 0.0);
        flatVofWave_0.getVerticalDirection().setComponents(0.0, 0.0, 1.0);
        flatVofWave_0.getCurrent().setDefinition("[-$Vm, 0.0, 0.0]");
        flatVofWave_0.getWind().setDefinition("[-$Vm, 0.0, 0.0]");
        flatVofWave_0.getHeavyFluidDensity().setDefinition("${rho}");
        SegregatedVofModel segregatedVofModel_0 = PhysicsField.getModelManager().getModel(SegregatedVofModel.class);
        HRICSchemeParameters hRICSchemeParameters_0 = segregatedVofModel_0.getHRICSchemeParameters();
        hRICSchemeParameters_0.getCFL_l().setValue(500.0);
        hRICSchemeParameters_0.getCFL_u().setValue(1000.0);
        // Initial Profile
        PrimitiveFieldFunction staticPressure = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("HydrostaticPressureWave0"));
        PrimitiveFieldFunction velocityWave = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("VelocityWave0"));
        PrimitiveFieldFunction vfWater = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("VolumeFractionHeavyFluidWave0"));
        InitialPressureProfile Profile_P = PhysicsField.getInitialConditions().get(InitialPressureProfile.class);
        Profile_P.setMethod(FunctionScalarProfileMethod.class);
        Profile_P.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(staticPressure);
        VelocityProfile Profile_v = PhysicsField.getInitialConditions().get(VelocityProfile.class);
        Profile_v.setMethod(FunctionVectorProfileMethod.class);
        Profile_v.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(velocityWave);
        VolumeFractionProfile Profile_vf = PhysicsField.getInitialConditions().get(VolumeFractionProfile.class);
        Profile_vf.setMethod(CompositeNMinus1ArrayProfileMethod.class);
        ScalarProfile scalarProfile_0 = Profile_vf.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
        scalarProfile_0.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The physics field creation completed");
    }
  }

  public static class BoundaryCondition extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create boundary condition program");
      try {
        mySim.println("@Try running create boundary condition program");
        PrimitiveFieldFunction staticPressure = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("HydrostaticPressureWave0"));
        PrimitiveFieldFunction velocityWave = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("VelocityWave0"));
        PrimitiveFieldFunction vfWater = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("VolumeFractionHeavyFluidWave0"));
        // Definition Boundary
        Region StaticDomain = mySim.getRegionManager().getRegion("01-Static Domain");
        Boundary bdy_In = StaticDomain.getBoundaryManager().getBoundary("Far Field.Inlet");
        Boundary bdy_Out = StaticDomain.getBoundaryManager().getBoundary("Far Field.Outlet");
        Boundary bdy_Top = StaticDomain.getBoundaryManager().getBoundary("Far Field.Top");
        Boundary bdy_Bot = StaticDomain.getBoundaryManager().getBoundary("Far Field.Bottom");
        Boundary bdy_Port = StaticDomain.getBoundaryManager().getBoundary("Far Field.PortSide");
        // Setup VOF Damping
        StaticDomain.getConditions().get(VofWaveZoneOption.class).setSelected(VofWaveZoneOption.Type.DAMPED);
        VofWaveDampingLength vofWaveDampingLength_0 = StaticDomain.getValues().get(VofWaveDampingLength.class);
        vofWaveDampingLength_0.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Lm}");
        bdy_In.getConditions().get(VofWaveDampingBoundaryOption.class).setSelected(VofWaveDampingBoundaryOption.Type.YES);
        bdy_Out.getConditions().get(VofWaveDampingBoundaryOption.class).setSelected(VofWaveDampingBoundaryOption.Type.YES);
        bdy_Port.getConditions().get(VofWaveDampingBoundaryOption.class).setSelected(VofWaveDampingBoundaryOption.Type.YES);
        // Inlet Profile
        InletBoundary inletBdy = mySim.get(ConditionTypeManager.class).get(InletBoundary.class);
        PressureBoundary pressureBdy = mySim.get(ConditionTypeManager.class).get(PressureBoundary.class);
        bdy_Out.setBoundaryType(pressureBdy);
        bdy_In.setBoundaryType(inletBdy);
        bdy_Top.setBoundaryType(inletBdy);
        bdy_Bot.setBoundaryType(inletBdy);
        bdy_Port.setBoundaryType(inletBdy);
        // Pressure Profile
        StaticPressureProfile Pressure_Out = bdy_Out.getValues().get(StaticPressureProfile.class);
        Pressure_Out.setMethod(FunctionScalarProfileMethod.class);
        Pressure_Out.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(staticPressure);
        // Velocity Profile
        bdy_In.getConditions().get(InletVelocityOption.class).setSelected(InletVelocityOption.Type.COMPONENTS);
        bdy_Top.getConditions().get(InletVelocityOption.class).setSelected(InletVelocityOption.Type.COMPONENTS);
        bdy_Bot.getConditions().get(InletVelocityOption.class).setSelected(InletVelocityOption.Type.COMPONENTS);
        bdy_Port.getConditions().get(InletVelocityOption.class).setSelected(InletVelocityOption.Type.COMPONENTS);
        VelocityProfile velocity_In = bdy_In.getValues().get(VelocityProfile.class);
        velocity_In.setMethod(FunctionVectorProfileMethod.class);
        VelocityProfile velocity_Top = bdy_Top.getValues().get(VelocityProfile.class);
        velocity_Top.setMethod(FunctionVectorProfileMethod.class);
        VelocityProfile velocity_Bot = bdy_Bot.getValues().get(VelocityProfile.class);
        velocity_Bot.setMethod(FunctionVectorProfileMethod.class);
        VelocityProfile velocity_Port = bdy_Port.getValues().get(VelocityProfile.class);
        velocity_Port.setMethod(FunctionVectorProfileMethod.class);
        velocity_In.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(velocityWave);
        velocity_Top.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(velocityWave);
        velocity_Bot.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(velocityWave);
        velocity_Port.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(velocityWave);
        // Volume Fraction
        VolumeFractionProfile VFP_In = bdy_In.getValues().get(VolumeFractionProfile.class);
        VFP_In.setMethod(CompositeNMinus1ArrayProfileMethod.class);
        VolumeFractionProfile VFP_Out = bdy_Out.getValues().get(VolumeFractionProfile.class);
        VFP_Out.setMethod(CompositeNMinus1ArrayProfileMethod.class);
        VolumeFractionProfile VFP_Top = bdy_Top.getValues().get(VolumeFractionProfile.class);
        VFP_Top.setMethod(CompositeNMinus1ArrayProfileMethod.class);
        VolumeFractionProfile VFP_Bot = bdy_Bot.getValues().get(VolumeFractionProfile.class);
        VFP_Bot.setMethod(CompositeNMinus1ArrayProfileMethod.class);
        VolumeFractionProfile VFP_Port = bdy_Port.getValues().get(VolumeFractionProfile.class);
        VFP_Port.setMethod(CompositeNMinus1ArrayProfileMethod.class);
        ScalarProfile vfIn = VFP_In.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
        vfIn.setMethod(FunctionScalarProfileMethod.class);
        ScalarProfile vfOut = VFP_Out.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
        vfOut.setMethod(FunctionScalarProfileMethod.class);
        ScalarProfile vfTOP = VFP_Top.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
        vfTOP.setMethod(FunctionScalarProfileMethod.class);
        ScalarProfile vfBot = VFP_Bot.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
        vfBot.setMethod(FunctionScalarProfileMethod.class);
        ScalarProfile vfPort = VFP_Port.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
        vfPort.setMethod(FunctionScalarProfileMethod.class);
        vfIn.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
        vfOut.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
        vfTOP.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
        vfBot.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
        vfPort.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
        // Symmetry Boundary
        SymmetryBoundary symmetryBdy = mySim.get(ConditionTypeManager.class).get(SymmetryBoundary.class);
        OversetMeshBoundary oversetMeshBdy = mySim.get(ConditionTypeManager.class).get(OversetMeshBoundary.class);
        Boundary bdy_Sym, bdy_Hull;
        Region ODomain;
        if (!Overset) {
          bdy_Sym = StaticDomain.getBoundaryManager().getBoundary("Far Field.Symmetry");
          bdy_Sym.setBoundaryType(symmetryBdy);
        } else if (!Propeller) {
          bdy_Sym = StaticDomain.getBoundaryManager().getBoundary("Far Field.Symmetry");
          bdy_Sym.setBoundaryType(symmetryBdy);
          ODomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          Boundary bdy_Overset = ODomain.getBoundaryManager().getBoundary("Overlap Field.Overset");
          Boundary bdy_OvSym = ODomain.getBoundaryManager().getBoundary("Overlap Field.Symmetry");
          // Boundary Condition
          bdy_OvSym.setBoundaryType(symmetryBdy);
          bdy_Overset.setBoundaryType(oversetMeshBdy);
          // Overset Mesh
          IndirectRegionInterface OversetInterface = mySim.getInterfaceManager().createIndirectRegionInterface(StaticDomain, ODomain, "Overset Mesh", false);
          OversetInterface.setUseAlternateHoleCutting(true);
          OversetInterface.setTreatErrorAsWarning(true);
          OversetInterface.setPresentationName("Overset Mesh");
        } else {
          bdy_Sym = StaticDomain.getBoundaryManager().getBoundary("Far Field.Starboard");
          bdy_Sym.setBoundaryType(inletBdy);
          bdy_Sym.getConditions().get(InletVelocityOption.class).setSelected(InletVelocityOption.Type.COMPONENTS);
          VelocityProfile velocity_Sym = bdy_Sym.getValues().get(VelocityProfile.class);
          velocity_Sym.setMethod(FunctionVectorProfileMethod.class);
          velocity_Sym.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(velocityWave);
          VolumeFractionProfile VFP_Sym = bdy_Sym.getValues().get(VolumeFractionProfile.class);
          VFP_Sym.setMethod(CompositeNMinus1ArrayProfileMethod.class);
          ScalarProfile vfSym = VFP_Sym.getMethod(CompositeNMinus1ArrayProfileMethod.class).getProfile(0);
          vfSym.setMethod(FunctionScalarProfileMethod.class);
          vfSym.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(vfWater);
          bdy_Sym.getConditions().get(VofWaveDampingBoundaryOption.class).setSelected(VofWaveDampingBoundaryOption.Type.YES);
          // Overset Boundary Condition
          ODomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          Boundary bdy_Overset = ODomain.getBoundaryManager().getBoundary("Overlap Field.Overset");
          bdy_Overset.setBoundaryType(oversetMeshBdy);
          // Overset Mesh
          IndirectRegionInterface OversetInterface = mySim.getInterfaceManager().createIndirectRegionInterface(StaticDomain, ODomain, "Overset Mesh", false);
          OversetInterface.setUseAlternateHoleCutting(true);
          OversetInterface.setTreatErrorAsWarning(true);
          OversetInterface.setPresentationName("Overset Mesh");
          // Interface
          Boundary bdy_InterO = ODomain.getBoundaryManager().getBoundary("Rotation Field.Interface");
          Region RDomain = mySim.getRegionManager().getRegion("03-Rotation Domain");
          Boundary bdy_InterR = RDomain.getBoundaryManager().getBoundary("Rotation Field.Interface");
          BoundaryInterface boundaryInterface_0 = mySim.getInterfaceManager().createBoundaryInterface(bdy_InterR, bdy_InterO, "Interface");
          boundaryInterface_0.setPresentationName("Interface");
        }
        // Motion
        if (Overset) {
          SixDofMotion sixDofMotion_0 = mySim.get(MotionManager.class).createMotion(SixDofMotion.class, "DFBI Rotation and Translation");
          ODomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          bdy_Hull = ODomain.getBoundaryManager().getBoundary("Ship.Hull");
          MotionSpecification motionSpecification_0 = ODomain.getValues().get(MotionSpecification.class);
          motionSpecification_0.setMotion(sixDofMotion_0);
          ContinuumBody continuumBody_0 = mySim.get(star.sixdof.BodyManager.class).createContinuumBody(true);
          continuumBody_0.setPresentationName("Ship");
          continuumBody_0.getBodySurface().setObjects(bdy_Hull);
          continuumBody_0.getBodyMass().setDefinition("${Mass}");
          continuumBody_0.getReleaseTime().setValue(1.0);
          continuumBody_0.getRampTime().setValue(5.0);
          ((BodyFreeMotion) continuumBody_0.getMotionType()).setFreeTranslationZ(true);
          ((BodyFreeMotion) continuumBody_0.getMotionType()).setFreeRotationY(true);
          CenterOfMass centerOfMass_0 = ((CenterOfMass) continuumBody_0.getInitialValueManager().getObject("Center of Mass"));
          LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
          CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
          centerOfMass_0.setCoordinateSystem(LCS_H);
          MomentOfInertia momentOfInertia_0 = ((MomentOfInertia) continuumBody_0.getInitialValueManager().getObject("Moment of Inertia"));
          momentOfInertia_0.setUseCenterOfMass(true);
          momentOfInertia_0.getDiagonalComponents().setDefinition("[${Mass}*pow(${Bm}, 2), ${Mass}*pow(${Lm}, 2), ${Mass}*pow(${Lm}, 2)]");
          Orientation orientation_0 = ((Orientation) continuumBody_0.getInitialValueManager().getObject("Orientation"));
          orientation_0.setCoordinateSystem(LCS_H);
          if (Propeller) {
            Region RDomain = mySim.getRegionManager().getRegion("03-Rotation Domain");
            Boundary bdy_BladeK = RDomain.getBoundaryManager().getBoundary("Propeller.Blade-Key");
            Boundary bdy_Blade = RDomain.getBoundaryManager().getBoundary("Propeller.Blade");
            Boundary bdy_Hub = RDomain.getBoundaryManager().getBoundary("Propeller.Hub");
            continuumBody_0.getBodySurface().setObjects(bdy_Hull, bdy_BladeK, bdy_Blade, bdy_Hub);
            Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            CartesianCoordinateSystem LCS_MH = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Ship-CSys"));
            CartesianCoordinateSystem LCS_MP = LCS_MH.getLocalCoordinateSystemManager().createLocalCoordinateSystem(CartesianCoordinateSystem.class, "Cartesian");
            LCS_MP.getOrigin().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[] {-72.8, 0.0, -9.0}));
            LCS_MP.setBasis0(new DoubleVector(new double[] {1.0, 0.0, 0.0}));
            LCS_MP.setBasis1(new DoubleVector(new double[] {0.0, 1.0, 0.0}));
            LCS_MP.setPresentationName("Propeller");
            RotatingMotion rotatingMotion_0 = mySim.get(MotionManager.class).createMotion(RotatingMotion.class, "Rotation");
            rotatingMotion_0.setCoordinateSystem(LCS_MP);
            rotatingMotion_0.getAxisDirection().setComponents(1.0, 0.0, 0.0);
            RotationRate rotationRate_0 = ((RotationRate) rotatingMotion_0.getRotationSpecification());
            rotationRate_0.getRotationRate().setDefinition("2*${PI}*${n}");
            MotionSpecification motionSpecification_1 = RDomain.getValues().get(MotionSpecification.class);
            motionSpecification_1.setMotion(sixDofMotion_0);
            RotatingReferenceFrame rotatingReferenceFrame_0 = ((RotatingReferenceFrame) mySim.get(ReferenceFrameManager.class).getObject("ReferenceFrame for Rotation"));
            motionSpecification_1.setReferenceFrame(rotatingReferenceFrame_0);
          }
        }
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The boundary condition creation completed");
    }
  }

  public static class MeshContinuum extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create mesh continuum program");
      try {
        mySim.println("@Try running create mesh continuum program");
        // Create < 01-Free Surface >
        LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
        MeshPartFactory meshPartFactory_0 = mySim.get(MeshPartFactory.class);
        SimpleBlockPart Part_Free = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
        Part_Free.setCoordinateSystem(GCS);
        Part_Free.getCorner1().setCoordinateSystem(GCS);
        Part_Free.getCorner1().setDefinition("[-2*${Cds}, -2*${Cds}, -2*${Fst}]");
        Part_Free.getCorner2().setCoordinateSystem(GCS);
        Part_Free.getCorner2().setDefinition("[2*${Cds}, 2*${Cds}, ${Fst}]");
        Part_Free.rebuildSimpleShapePart();
        Part_Free.setPresentationName("Free Surface");
        // Create < 02-Turbulence Wake >
        SimpleBlockPart Part_Turb = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
        Part_Turb.setCoordinateSystem(GCS);
        Part_Turb.getCorner1().setCoordinateSystem(GCS);
        Part_Turb.getCorner1().setDefinition("[-0.5*${Lm}, -${Bm}, -2*${Tm}]");
        Part_Turb.getCorner2().setCoordinateSystem(GCS);
        Part_Turb.getCorner2().setDefinition("[1.1*${Lm}, ${Bm}, 0.5*${Tm}]");
        Part_Turb.rebuildSimpleShapePart();
        Part_Turb.setPresentationName("Turbulence Wake");
        // Create < 03-Kelvin Wave >
        Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        SimpleConePart Part_ConeK = meshPartFactory_0.createNewConePart(mySim.get(SimulationPartManager.class));
        Part_ConeK.setCoordinateSystem(GCS);
        Part_ConeK.getStartCoordinate().setCoordinateSystem(GCS);
        Part_ConeK.getStartCoordinate().setDefinition("[1.1*${Lm}, 0.0, 0.0]");
        Part_ConeK.getStartRadius().setUnits(units_m);
        Part_ConeK.getStartRadius().setDefinition("${Bm}");
        Part_ConeK.getEndCoordinate().setCoordinateSystem(GCS);
        Part_ConeK.getEndCoordinate().setDefinition("[-2*${Lm}, 0.0, 0.0]");
        Part_ConeK.getEndRadius().setUnits(units_m);
        Part_ConeK.getEndRadius().setDefinition("3.1*${Lm}*tan(0.3365)+${Bm}");
        Part_ConeK.getTessellationDensityOption().setSelected(TessellationDensityOption.Type.MEDIUM);
        Part_ConeK.rebuildSimpleShapePart();
        Part_ConeK.setPresentationName("Cone-Kelvin Wave");
        IntersectPartsOperation Operation_Kelvin = (IntersectPartsOperation) mySim.get(MeshOperationManager.class).createIntersectPartsOperation(new NeoObjectVector(new Object[]{Part_ConeK, Part_Free}));
        Operation_Kelvin.setPerformCADBoolean(true);
        Operation_Kelvin.execute();
        Operation_Kelvin.setPresentationName("Kelvin Wave");
        MeshOperationPart Part_Kelvin = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Intersect"));
        Part_Kelvin.setPresentationName("Kelvin Wave");
        // Create < 04-Bow & Stern >
        SimpleBlockPart Part_Bow = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
        Part_Bow.setCoordinateSystem(GCS);
        Part_Bow.getCorner1().setCoordinateSystem(GCS);
        Part_Bow.getCorner1().setDefinition("[0.85*${Lm}, -0.25*${Bm}, -1.2*${Tm}]");
        Part_Bow.getCorner2().setCoordinateSystem(GCS);
        Part_Bow.getCorner2().setDefinition("[1.00*${Lm}, 0.25*${Bm}, 0.5*${Tm}]");
        Part_Bow.rebuildSimpleShapePart();
        Part_Bow.setPresentationName("Bow");
        SimpleBlockPart Part_Stern = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
        Part_Stern.setCoordinateSystem(GCS);
        Part_Stern.getCorner1().setCoordinateSystem(GCS);
        Part_Stern.getCorner1().setDefinition("[-0.05*${Lm}, -0.25*${Bm}, -1.2*${Tm}]");
        Part_Stern.getCorner2().setCoordinateSystem(GCS);
        Part_Stern.getCorner2().setDefinition("[0.15*${Lm}, 0.25*${Bm}, 0.5*${Tm}]");
        Part_Stern.rebuildSimpleShapePart();
        Part_Stern.setPresentationName("Stern");
        // Create < 01-Static Domain Mesh >
        MeshOperationPart Part_StaticDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("01-Static Domain"));
        AutoMeshOperation StaticDomainMesh = mySim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.trimmer.TrimmerAutoMesher", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{Part_StaticDomain}));
        StaticDomainMesh.setPresentationName("01-Static Domain Mesh");
        StaticDomainMesh.getMesherParallelModeOption().setSelected(MesherParallelModeOption.Type.PARALLEL);
        TrimmerAutoMesher trimmerAutoMesher_1 = ((TrimmerAutoMesher) StaticDomainMesh.getMeshers().getObject("Trimmed Cell Mesher"));
        trimmerAutoMesher_1.setDoMeshAlignment(true);
        MeshAlignmentLocation meshAlignmentLocation_1 = StaticDomainMesh.getDefaultValues().get(MeshAlignmentLocation.class);
        meshAlignmentLocation_1.getLocation().setComponents(0.0, 0.0, 0.0);
        StaticDomainMesh.getDefaultValues().get(BaseSize.class).setDefinition("floor(${Lm}/50)");
        PartsMinimumSurfaceSize partsMinimumSurfaceSize_1 = StaticDomainMesh.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        partsMinimumSurfaceSize_1.getRelativeSizeScalar().setValue(6.25);
        SurfaceCurvature surfaceCurvature_1 = StaticDomainMesh.getDefaultValues().get(SurfaceCurvature.class);
        surfaceCurvature_1.setEnableCurvatureDeviationDist(true);
        surfaceCurvature_1.setNumPointsAroundCircle(72.0);
        surfaceCurvature_1.getCurvatureDeviationDistance().setValue(0.005);
        PartsSimpleTemplateGrowthRate partsSimpleTemplateGrowthRate_1 = StaticDomainMesh.getDefaultValues().get(PartsSimpleTemplateGrowthRate.class);
        partsSimpleTemplateGrowthRate_1.getGrowthRateOption().setSelected(PartsGrowthRateOption.Type.VERYSLOW);
        MaximumCellSize maximumCellSize_1 = StaticDomainMesh.getDefaultValues().get(MaximumCellSize.class);
        maximumCellSize_1.getRelativeSizeScalar().setValue(800.0);
        // Prism Layers
        PrismAutoMesher prismAutoMesher_1 = ((PrismAutoMesher) StaticDomainMesh.getMeshers().getObject("Prism Layer Mesher"));
        prismAutoMesher_1.setMinimumThickness(5.0);
        prismAutoMesher_1.setLayerChoppingPercentage(25.0);
        prismAutoMesher_1.setBoundaryMarchAngle(75.0);
        prismAutoMesher_1.setNearCoreLayerAspectRatio(0.6);
        NumPrismLayers numPrismLayers_1 = StaticDomainMesh.getDefaultValues().get(NumPrismLayers.class);
        IntegerValue integerValue_1 = numPrismLayers_1.getNumLayersValue();
        integerValue_1.getQuantity().setDefinition("${Pn}");
        PrismLayerStretching prismLayerStretching_1 = StaticDomainMesh.getDefaultValues().get(PrismLayerStretching.class);
        prismLayerStretching_1.getStretchingQuantity().setDefinition("${Ps}");
        PrismThickness prismThickness_1 = StaticDomainMesh.getDefaultValues().get(PrismThickness.class);
        prismThickness_1.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        prismThickness_1.getAbsoluteSizeValue().setDefinition("${Pt}");
        // Create Surface Control -- Far Field
        PartSurface SDomain_In = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Inlet");
        PartSurface SDomain_Out = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Outlet");
        PartSurface SDomain_Top = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Top");
        PartSurface SDomain_Bot = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Bottom");
        PartSurface SDomain_Port = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.PortSide");
        SurfaceCustomMeshControl Mesh_Far = StaticDomainMesh.getCustomMeshControls().createSurfaceControl();
        Mesh_Far.setPresentationName("Boundary");
        PartSurface SDomain_Sym;
        if (Overset && Propeller) {
          SDomain_Sym = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Starboard");
        } else {
          SDomain_Sym = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Symmetry");
        }
        Mesh_Far.getGeometryObjects().setObjects(SDomain_In, SDomain_Out, SDomain_Top, SDomain_Bot, SDomain_Port, SDomain_Sym);
        Mesh_Far.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        Mesh_Far.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
        PartsCustomizePrismMesh MehsP_Far = Mesh_Far.getCustomConditions().get(PartsCustomizePrismMesh.class);
        MehsP_Far.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.DISABLE);
        PartsTargetSurfaceSize MeshT_Far = Mesh_Far.getCustomValues().get(PartsTargetSurfaceSize.class);
        MeshT_Far.getRelativeSizeScalar().setValue(800.0);
        PartsMinimumSurfaceSize MeshM_Far = Mesh_Far.getCustomValues().get(PartsMinimumSurfaceSize.class);
        MeshM_Far.getRelativeSizeScalar().setValue(800.0);
        if (!Overset) {
          // Create Surface Control -- Hull
          PartSurface StaticDomain_Hull = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Ship.Hull");
          SurfaceCustomMeshControl Mesh_Hull = StaticDomainMesh.getCustomMeshControls().createSurfaceControl();
          Mesh_Hull.setPresentationName("Hull");
          Mesh_Hull.getGeometryObjects().setObjects(StaticDomain_Hull);
          Mesh_Hull.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
          Mesh_Hull.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
          PartsTargetSurfaceSize MeshT_Hull = Mesh_Hull.getCustomValues().get(PartsTargetSurfaceSize.class);
          MeshT_Hull.getRelativeSizeScalar().setValue(25.0);
          PartsMinimumSurfaceSize MeshM_Hull = Mesh_Hull.getCustomValues().get(PartsMinimumSurfaceSize.class);
          MeshM_Hull.getRelativeSizeScalar().setValue(25.0);
          Mesh_Hull.getCustomConditions().get(PartsCustomSurfaceGrowthRateOption.class).setSelected(PartsCustomSurfaceGrowthRateOption.Type.CUSTOM);
          PartsCustomSimpleSurfaceGrowthRate MeshRate_Hull = Mesh_Hull.getCustomValues().get(PartsCustomSimpleSurfaceGrowthRate.class);
          MeshRate_Hull.getSurfaceGrowthRateOption().setSelected(PartsSurfaceGrowthRateOption.Type.MEDIUM);
          // Create Surface Control -- Rudder
          SurfaceCustomMeshControl Mesh_Rudder = StaticDomainMesh.getCustomMeshControls().createSurfaceControl();
          Mesh_Rudder.setPresentationName("Rudder");
          Mesh_Rudder.getGeometryObjects().setObjects();
          Mesh_Rudder.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
          Mesh_Rudder.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
          PartsTargetSurfaceSize MeshT_Rudder = Mesh_Rudder.getCustomValues().get(PartsTargetSurfaceSize.class);
          MeshT_Rudder.getRelativeSizeScalar().setValue(6.25);
          PartsMinimumSurfaceSize MeshM_Rudder = Mesh_Rudder.getCustomValues().get(PartsMinimumSurfaceSize.class);
          MeshM_Rudder.getRelativeSizeScalar().setValue(6.25);
          Mesh_Rudder.getCustomConditions().get(PartsCustomSurfaceGrowthRateOption.class).setSelected(PartsCustomSurfaceGrowthRateOption.Type.CUSTOM);
          PartsCustomSimpleSurfaceGrowthRate MeshRate_Rudder = Mesh_Rudder.getCustomValues().get(PartsCustomSimpleSurfaceGrowthRate.class);
          MeshRate_Rudder.getSurfaceGrowthRateOption().setSelected(PartsSurfaceGrowthRateOption.Type.SLOW);
          Mesh_Rudder.setEnableControl(false);
          // Create Volume Control -- Bow & Stern
          VolumeCustomMeshControl Mesh_Bow = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
          Mesh_Bow.setPresentationName("04-Bow & Stern");
          Mesh_Bow.getGeometryObjects().setObjects(Part_Bow, Part_Stern);
          VolumeControlTrimmerSizeOption MeshOp_Bow = Mesh_Bow.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
          MeshOp_Bow.setVolumeControlBaseSizeOption(true);
          VolumeControlSize MeshS_Bow = Mesh_Bow.getCustomValues().get(VolumeControlSize.class);
          MeshS_Bow.getRelativeSizeScalar().setValue(25.0);
          Mesh_Bow.setEnableControl(true);
        } else {
          // Create < 05-Overlap >
          SimpleBlockPart Part_Overlap = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
          Part_Overlap.setCoordinateSystem(GCS);
          Part_Overlap.getCorner1().setCoordinateSystem(GCS);
          Part_Overlap.getCorner1().setDefinition("[-0.2*${Lm}, -1.12*${Bm}, -2.0*${Dm}]");
          Part_Overlap.getCorner2().setCoordinateSystem(GCS);
          Part_Overlap.getCorner2().setDefinition("[1.2*${Lm}, 1.12*${Bm}, 2.0*${Dm}]");
          Part_Overlap.rebuildSimpleShapePart();
          Part_Overlap.setPresentationName("Overlapping Motion");
          VolumeCustomMeshControl Mesh_Over = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
          Mesh_Over.setPresentationName("05-Overlap");
          Mesh_Over.getGeometryObjects().setObjects(Part_Overlap);
          VolumeControlTrimmerSizeOption MeshOp_Over = Mesh_Over.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
          MeshOp_Over.setVolumeControlBaseSizeOption(true);
          VolumeControlSize MeshS_Over = Mesh_Over.getCustomValues().get(VolumeControlSize.class);
          MeshS_Over.getRelativeSizeScalar().setValue(50.0);
          Mesh_Over.setEnableControl(true);
          // 02-Oveset Domain Mesh
          MeshOperationPart Part_ODomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("02-Overset Domain"));
          AutoMeshOperation OversetDomainMesh = mySim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.trimmer.TrimmerAutoMesher", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{Part_ODomain}));
          OversetDomainMesh.setPresentationName("02-Oveset Domain Mesh");
          OversetDomainMesh.getMesherParallelModeOption().setSelected(MesherParallelModeOption.Type.PARALLEL);
          TrimmerAutoMesher trimmerAutoMesher_2 = ((TrimmerAutoMesher) OversetDomainMesh.getMeshers().getObject("Trimmed Cell Mesher"));
          trimmerAutoMesher_2.setDoMeshAlignment(true);
          MeshAlignmentLocation meshAlignmentLocation_2 = OversetDomainMesh.getDefaultValues().get(MeshAlignmentLocation.class);
          meshAlignmentLocation_2.getLocation().setComponents(0.0, 0.0, 0.0);
          OversetDomainMesh.getDefaultValues().get(BaseSize.class).setDefinition("floor(${Lm}/50)");
          PartsMinimumSurfaceSize partsMinimumSurfaceSize_2 = OversetDomainMesh.getDefaultValues().get(PartsMinimumSurfaceSize.class);
          partsMinimumSurfaceSize_2.getRelativeSizeScalar().setValue(6.25);
          SurfaceCurvature surfaceCurvature_2 = OversetDomainMesh.getDefaultValues().get(SurfaceCurvature.class);
          surfaceCurvature_2.setEnableCurvatureDeviationDist(true);
          surfaceCurvature_2.setNumPointsAroundCircle(72.0);
          surfaceCurvature_2.getCurvatureDeviationDistance().setValue(0.005);
          PartsSimpleTemplateGrowthRate partsSimpleTemplateGrowthRate_2 = OversetDomainMesh.getDefaultValues().get(PartsSimpleTemplateGrowthRate.class);
          partsSimpleTemplateGrowthRate_2.getGrowthRateOption().setSelected(PartsGrowthRateOption.Type.SLOW);
          MaximumCellSize maximumCellSize_2 = OversetDomainMesh.getDefaultValues().get(MaximumCellSize.class);
          maximumCellSize_2.getRelativeSizeScalar().setValue(100.0);
          PrismAutoMesher prismAutoMesher_2 = ((PrismAutoMesher) OversetDomainMesh.getMeshers().getObject("Prism Layer Mesher"));
          prismAutoMesher_2.setMinimumThickness(5.0);
          prismAutoMesher_2.setLayerChoppingPercentage(25.0);
          prismAutoMesher_2.setBoundaryMarchAngle(75.0);
          prismAutoMesher_2.setNearCoreLayerAspectRatio(0.6);
          NumPrismLayers numPrismLayers_2 = OversetDomainMesh.getDefaultValues().get(NumPrismLayers.class);
          IntegerValue integerValue_2 = numPrismLayers_2.getNumLayersValue();
          integerValue_2.getQuantity().setDefinition("${Pn}");
          PrismLayerStretching prismLayerStretching_2 = OversetDomainMesh.getDefaultValues().get(PrismLayerStretching.class);
          prismLayerStretching_2.getStretchingQuantity().setDefinition("${Ps}");
          PrismThickness prismThickness_2 = OversetDomainMesh.getDefaultValues().get(PrismThickness.class);
          prismThickness_2.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
          prismThickness_2.getAbsoluteSizeValue().setDefinition("${Pt}");
          // Create Surface Control -- Hull
          PartSurface ODomain_Hull = Part_ODomain.getPartSurfaceManager().getPartSurface("Ship.Hull");
          SurfaceCustomMeshControl Mesh_Hull = OversetDomainMesh.getCustomMeshControls().createSurfaceControl();
          Mesh_Hull.setPresentationName("Hull");
          Mesh_Hull.getGeometryObjects().setObjects(ODomain_Hull);
          Mesh_Hull.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
          Mesh_Hull.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
          PartsTargetSurfaceSize MeshT_Hull = Mesh_Hull.getCustomValues().get(PartsTargetSurfaceSize.class);
          MeshT_Hull.getRelativeSizeScalar().setValue(25.0);
          PartsMinimumSurfaceSize MeshM_Hull = Mesh_Hull.getCustomValues().get(PartsMinimumSurfaceSize.class);
          MeshM_Hull.getRelativeSizeScalar().setValue(25.0);
          Mesh_Hull.getCustomConditions().get(PartsCustomSurfaceGrowthRateOption.class).setSelected(PartsCustomSurfaceGrowthRateOption.Type.CUSTOM);
          PartsCustomSimpleSurfaceGrowthRate MeshRate_Hull = Mesh_Hull.getCustomValues().get(PartsCustomSimpleSurfaceGrowthRate.class);
          MeshRate_Hull.getSurfaceGrowthRateOption().setSelected(PartsSurfaceGrowthRateOption.Type.MEDIUM);
          // Create Surface Control -- Rudder
          SurfaceCustomMeshControl Mesh_Rudder = OversetDomainMesh.getCustomMeshControls().createSurfaceControl();
          Mesh_Rudder.setPresentationName("Rudder");
          Mesh_Rudder.getGeometryObjects().setObjects();
          Mesh_Rudder.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
          Mesh_Rudder.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
          PartsTargetSurfaceSize MeshT_Rudder = Mesh_Rudder.getCustomValues().get(PartsTargetSurfaceSize.class);
          MeshT_Rudder.getRelativeSizeScalar().setValue(6.25);
          PartsMinimumSurfaceSize MeshM_Rudder = Mesh_Rudder.getCustomValues().get(PartsMinimumSurfaceSize.class);
          MeshM_Rudder.getRelativeSizeScalar().setValue(6.25);
          Mesh_Rudder.getCustomConditions().get(PartsCustomSurfaceGrowthRateOption.class).setSelected(PartsCustomSurfaceGrowthRateOption.Type.CUSTOM);
          PartsCustomSimpleSurfaceGrowthRate MeshRate_Rudder = Mesh_Rudder.getCustomValues().get(PartsCustomSimpleSurfaceGrowthRate.class);
          MeshRate_Rudder.getSurfaceGrowthRateOption().setSelected(PartsSurfaceGrowthRateOption.Type.SLOW);
          Mesh_Rudder.setEnableControl(false);
          // Create Volume Control -- Bow & Stern
          VolumeCustomMeshControl Mesh_Bow = OversetDomainMesh.getCustomMeshControls().createVolumeControl();
          Mesh_Bow.setPresentationName("04-Bow & Stern");
          Mesh_Bow.getGeometryObjects().setObjects(Part_Bow, Part_Stern);
          VolumeControlTrimmerSizeOption MeshOp_Bow = Mesh_Bow.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
          MeshOp_Bow.setVolumeControlBaseSizeOption(true);
          VolumeControlSize MeshS_Bow = Mesh_Bow.getCustomValues().get(VolumeControlSize.class);
          MeshS_Bow.getRelativeSizeScalar().setValue(25.0);
          Mesh_Bow.setEnableControl(true);
          if (Propeller) {
            // 02-Overset Domain : Interface
            PartSurface ODomain_Inter = Part_ODomain.getPartSurfaceManager().getPartSurface("Rotation Field.Interface");
            SurfaceCustomMeshControl Mesh_InterS = OversetDomainMesh.getCustomMeshControls().createSurfaceControl();
            Mesh_InterS.setPresentationName("Interface");
            Mesh_InterS.getGeometryObjects().setObjects(ODomain_Inter);
            Mesh_InterS.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            Mesh_InterS.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            PartsTargetSurfaceSize MeshT_InterS = Mesh_InterS.getCustomValues().get(PartsTargetSurfaceSize.class);
            MeshT_InterS.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            MeshT_InterS.getAbsoluteSizeValue().setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            PartsMinimumSurfaceSize MeshM_InterS = Mesh_InterS.getCustomValues().get(PartsMinimumSurfaceSize.class);
            MeshM_InterS.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            MeshM_InterS.getAbsoluteSizeValue().setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            PartsCustomizePrismMesh partsCustomizePrismMesh_IS = Mesh_InterS.getCustomConditions().get(PartsCustomizePrismMesh.class);
            partsCustomizePrismMesh_IS.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
            PartsCustomizePrismMeshControls partsCustomizePrismMeshControls_IS = partsCustomizePrismMesh_IS.getCustomPrismControls();
            partsCustomizePrismMeshControls_IS.setCustomizeNumLayers(true);
            partsCustomizePrismMeshControls_IS.setCustomizeTotalThickness(true);
            NumPrismLayers numPrismLayers_IS = Mesh_InterS.getCustomValues().get(CustomPrismValuesManager.class).get(NumPrismLayers.class);
            IntegerValue integerValue_IS = numPrismLayers_IS.getNumLayersValue();
            integerValue_IS.getQuantity().setValue(1.0);
            PrismThickness prismThickness_IS = Mesh_InterS.getCustomValues().get(CustomPrismValuesManager.class).get(PrismThickness.class);
            prismThickness_IS.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            prismThickness_IS.getAbsoluteSizeValue().setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            // Create Rotation Domain Mesh
            CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
            CartesianCoordinateSystem LCS_P = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
            MeshOperationPart Part_RotationDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("03-Rotation Domain"));
            AutoMeshOperation RotationDomainMesh = mySim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.trimmer.TrimmerAutoMesher", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{Part_RotationDomain}));
            RotationDomainMesh.setPresentationName("03-Rotation Domain Mesh");
            RotationDomainMesh.getMesherParallelModeOption().setSelected(MesherParallelModeOption.Type.PARALLEL);
            TrimmerAutoMesher trimmerAutoMesher_R = ((TrimmerAutoMesher) RotationDomainMesh.getMeshers().getObject("Trimmed Cell Mesher"));
            trimmerAutoMesher_R.setDoMeshAlignment(true);
            MeshAlignmentLocation meshAlignmentLocation_R = RotationDomainMesh.getDefaultValues().get(MeshAlignmentLocation.class);
            meshAlignmentLocation_R.getLocation().setComponents(0.0, 0.0, 0.0);
            PrismAutoMesher prismAutoMesher_R = ((PrismAutoMesher) RotationDomainMesh.getMeshers().getObject("Prism Layer Mesher"));
            prismAutoMesher_R.setMinimumThickness(1.0);
            prismAutoMesher_R.setLayerChoppingPercentage(5.0);
            prismAutoMesher_R.setBoundaryMarchAngle(25.0);
            prismAutoMesher_R.setNearCoreLayerAspectRatio(0.5);
            RotationDomainMesh.getDefaultValues().get(BaseSize.class).setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            PartsMinimumSurfaceSize partsMinimumSurfaceSize_R = RotationDomainMesh.getDefaultValues().get(PartsMinimumSurfaceSize.class);
            partsMinimumSurfaceSize_R.getRelativeSizeScalar().setValue(1.0);
            NumPrismLayers numPrismLayers_Rotation = RotationDomainMesh.getDefaultValues().get(NumPrismLayers.class);
            IntegerValue integerValue_R = numPrismLayers_Rotation.getNumLayersValue();
            integerValue_R.getQuantity().setDefinition("${Pn}");
            PrismLayerStretching prismLayerStretching_R = RotationDomainMesh.getDefaultValues().get(PrismLayerStretching.class);
            prismLayerStretching_R.getStretchingQuantity().setDefinition("${Ps}");
            PrismThickness prismThickness_R = RotationDomainMesh.getDefaultValues().get(PrismThickness.class);
            prismThickness_R.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            prismThickness_R.getAbsoluteSizeValue().setDefinition("${Pt}");
            PartsSimpleTemplateGrowthRate partsSimpleTemplateGrowthRate_R = RotationDomainMesh.getDefaultValues().get(PartsSimpleTemplateGrowthRate.class);
            partsSimpleTemplateGrowthRate_R.getGrowthRateOption().setSelected(PartsGrowthRateOption.Type.FAST);
            MaximumCellSize maximumCellSize_R = RotationDomainMesh.getDefaultValues().get(MaximumCellSize.class);
            maximumCellSize_R.getRelativeSizeScalar().setValue(100.0);
            // Create Propeller Refine
            SimpleCylinderPart Part_RotationField = ((SimpleCylinderPart) mySim.get(SimulationPartManager.class).getPart("Rotation Field"));
            SimpleCylinderPart Propeller_Refine = (SimpleCylinderPart) Part_RotationField.duplicatePart(mySim.get(SimulationPartManager.class));
            mySim.get(SimulationPartManager.class).scaleParts(new NeoObjectVector(new Object[]{Propeller_Refine}), new DoubleVector(new double[]{1.2, 1.2, 1.2}), LCS_P);
            Propeller_Refine.setPresentationName("Propeller Refine");
            // 03-Rotation Domain : Interface
            MeshOperationPart Part_RDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("03-Rotation Domain"));
            PartSurface RDomain_Inter = Part_RDomain.getPartSurfaceManager().getPartSurface("Rotation Field.Interface");
            SurfaceCustomMeshControl Mesh_InterR = RotationDomainMesh.getCustomMeshControls().createSurfaceControl();
            Mesh_InterR.setPresentationName("Interface");
            Mesh_InterR.getGeometryObjects().setObjects(RDomain_Inter);
            Mesh_InterR.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            Mesh_InterR.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            PartsTargetSurfaceSize partsTargetSurfaceSize_IR = Mesh_InterR.getCustomValues().get(PartsTargetSurfaceSize.class);
            partsTargetSurfaceSize_IR.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            partsTargetSurfaceSize_IR.getAbsoluteSizeValue().setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            PartsMinimumSurfaceSize partsMinimumSurfaceSize_IR = Mesh_InterR.getCustomValues().get(PartsMinimumSurfaceSize.class);
            partsMinimumSurfaceSize_IR.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            partsMinimumSurfaceSize_IR.getAbsoluteSizeValue().setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            PartsCustomizePrismMesh partsCustomizePrismMesh_IR = Mesh_InterR.getCustomConditions().get(PartsCustomizePrismMesh.class);
            partsCustomizePrismMesh_IR.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
            PartsCustomizePrismMeshControls partsCustomizePrismMeshControls_IR = partsCustomizePrismMesh_IR.getCustomPrismControls();
            partsCustomizePrismMeshControls_IR.setCustomizeNumLayers(true);
            partsCustomizePrismMeshControls_IR.setCustomizeTotalThickness(true);
            NumPrismLayers numPrismLayers_IR = Mesh_InterR.getCustomValues().get(CustomPrismValuesManager.class).get(NumPrismLayers.class);
            IntegerValue integerValue_IR = numPrismLayers_IR.getNumLayersValue();
            integerValue_IR.getQuantity().setValue(1.0);
            PrismThickness prismThickness_IR = Mesh_InterR.getCustomValues().get(CustomPrismValuesManager.class).get(PrismThickness.class);
            prismThickness_IR.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
            prismThickness_IR.getAbsoluteSizeValue().setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
            // 03-Rotation Domain : Blade
            PartSurface RDomain_BladeK = Part_RDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade-Key");
            PartSurface RDomain_Blade = Part_RDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade");
            PartSurface RDomain_Hub = Part_RDomain.getPartSurfaceManager().getPartSurface("Propeller.Hub");
            SurfaceCustomMeshControl Mesh_Blade = RotationDomainMesh.getCustomMeshControls().createSurfaceControl();
            Mesh_Blade.setPresentationName("Blade");
            Mesh_Blade.getGeometryObjects().setObjects(RDomain_BladeK, RDomain_Blade, RDomain_Hub);
            Mesh_Blade.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            Mesh_Blade.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            PartsTargetSurfaceSize MeshT_Blade = Mesh_Blade.getCustomValues().get(PartsTargetSurfaceSize.class);
            MeshT_Blade.getRelativeSizeScalar().setValue(25.0);
            PartsMinimumSurfaceSize MeshM_Blade = Mesh_Blade.getCustomValues().get(PartsMinimumSurfaceSize.class);
            MeshM_Blade.getRelativeSizeScalar().setValue(5.0);
            // 02-Rotation Domain : Blade Edges
            PartCurve RDomain_Edges = Part_RDomain.getPartCurveManager().getPartCurve("Propeller.Blade Edges");
            CurveCustomMeshControl Mesh_Edges = RotationDomainMesh.getCustomMeshControls().createCurveControl();
            Mesh_Edges.setPresentationName("Blade Edges");
            Mesh_Edges.getGeometryObjects().setObjects(RDomain_Edges);
            Mesh_Edges.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            Mesh_Edges.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            PartsTargetSurfaceSize MeshT_Edges = Mesh_Edges.getCustomValues().get(PartsTargetSurfaceSize.class);
            MeshT_Edges.getRelativeSizeScalar().setValue(5.0);
            PartsMinimumSurfaceSize MehsM_Edges = Mesh_Edges.getCustomValues().get(PartsMinimumSurfaceSize.class);
            MehsM_Edges.getRelativeSizeScalar().setValue(1.0);
          }
        }
        // Create Volume Control -- Free Surface
        VolumeCustomMeshControl Mesh_Free = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
        Mesh_Free.setPresentationName("01-Free Surface");
        Mesh_Free.getGeometryObjects().setObjects(Part_Free);
        VolumeControlTrimmerSizeOption MeshOp_Free = Mesh_Free.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
        MeshOp_Free.setTrimmerAnisotropicSizeOption(true);
        TrimmerAnisotropicSize MeshS_Free = Mesh_Free.getCustomValues().get(TrimmerAnisotropicSize.class);
        MeshS_Free.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        MeshS_Free.setZSize(true);
        AbsoluteAnisotropicSizeZ MeshSz_Free = MeshS_Free.getAbsoluteZSize();
        MeshSz_Free.getValue().setDefinition("3 * ${Fst} / 20");
        // Create Volume Control -- Turbulence Wave
        VolumeCustomMeshControl Mesh_Turb = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
        Mesh_Turb.setPresentationName("02-Turbulence Wave");
        Mesh_Turb.getGeometryObjects().setObjects(Part_Turb);
        VolumeControlTrimmerSizeOption MeshOp_Turb = Mesh_Turb.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
        MeshOp_Turb.setVolumeControlBaseSizeOption(true);
        VolumeControlSize MeshS_Turb = Mesh_Turb.getCustomValues().get(VolumeControlSize.class);
        MeshS_Turb.getRelativeSizeScalar().setValue(50.0);
        Mesh_Turb.setEnableControl(true);
        // Create Volume Control -- Kelvin Wave
        VolumeCustomMeshControl Mesh_Kelvin = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
        Mesh_Kelvin.setPresentationName("03-Kelvin Wave");
        Mesh_Kelvin.getGeometryObjects().setObjects(Part_Kelvin);
        VolumeControlTrimmerSizeOption NeshOp_Kelvin = Mesh_Kelvin.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
        NeshOp_Kelvin.setTrimmerAnisotropicSizeOption(true);
        TrimmerAnisotropicSize MeshS_Kelvin = Mesh_Kelvin.getCustomValues().get(TrimmerAnisotropicSize.class);
        MeshS_Kelvin.setXSize(true);
        MeshS_Kelvin.setYSize(true);
        RelativeAnisotropicSizeX MeshSx_Kelvin = MeshS_Kelvin.getRelativeXSize();
        MeshSx_Kelvin.getRelativeSize().setValue(50.0);
        RelativeAnisotropicSizeY MeshSy_Kelvin = MeshS_Kelvin.getRelativeYSize();
        MeshSy_Kelvin.getRelativeSize().setValue(50.0);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The mesh continuum creation completed");
    }
  }

  public static class GenerateMesh extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is generate mesh program");
      try {
        mySim.println("@Try running generate mesh program");
        Region SDomain, ODomain, RDomain;
        Boundary bdy_Sym, bdy_Hull;
        SDomain = mySim.getRegionManager().getRegion("01-Static Domain");
        PlaneSection MidSection, WaterPlane, LongitudinalSection;
        // Generating Grid
        MeshPipelineController meshPipelineController_0 = mySim.get(MeshPipelineController.class);
        meshPipelineController_0.generateVolumeMesh();
        // Create < 02-Mesh >
        mySim.getSceneManager().createEmptyScene("Scene");
        Scene scene_2 = mySim.getSceneManager().getScene("Scene 1");
        scene_2.initializeAndWait();
        scene_2.resetCamera();
        scene_2.setPresentationName("02-Mesh");
        PartDisplayer partDisplayer_2 = scene_2.getDisplayerManager().createPartDisplayer("Geometry", -1, 4);
        partDisplayer_2.initialize();
        partDisplayer_2.setPresentationName("Mesh");
        partDisplayer_2.setOutline(false);
        partDisplayer_2.setMesh(true);
        partDisplayer_2.setSurface(true);
        if (!Overset) {
          bdy_Sym = SDomain.getBoundaryManager().getBoundary("Far Field.Symmetry");
          bdy_Hull = SDomain.getBoundaryManager().getBoundary("Ship.Hull");
          // Section Plane
          MidSection = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain}), new DoubleVector(new double[]{1.0, 0.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          MidSection.setPresentationName("Mid Section");
          MidSection.getOriginCoordinate().setDefinition("[0.5*${Lm}, 0.0, 0.0]");
          WaterPlane = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain}), new DoubleVector(new double[]{0.0, 0.0, 1.0}), new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          WaterPlane.setPresentationName("Water Plane");
          LongitudinalSection = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain}), new DoubleVector(new double[]{0.0, 1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.001, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          LongitudinalSection.setPresentationName("Longitudinal Section");
          partDisplayer_2.getInputParts().setObjects(bdy_Hull, bdy_Sym, MidSection, WaterPlane);
        } else if (!Propeller){
          ODomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          bdy_Sym = SDomain.getBoundaryManager().getBoundary("Far Field.Starboard");
          bdy_Hull = ODomain.getBoundaryManager().getBoundary("Ship.Hull");
          // Section Plane
          MidSection = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain, ODomain}), new DoubleVector(new double[]{1.0, 0.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          MidSection.setPresentationName("Mid Section");
          MidSection.getOriginCoordinate().setDefinition("[0.5*${Lm}, 0.0, 0.0]");
          WaterPlane = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain, ODomain}), new DoubleVector(new double[]{0.0, 0.0, 1.0}), new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          WaterPlane.setPresentationName("Water Plane");
          LongitudinalSection = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain, ODomain}), new DoubleVector(new double[]{0.0, 1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.001, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          LongitudinalSection.setPresentationName("Longitudinal Section");
          partDisplayer_2.getInputParts().setObjects(bdy_Hull, bdy_Sym, MidSection, WaterPlane);
        } else {
          ODomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          bdy_Hull = ODomain.getBoundaryManager().getBoundary("Ship.Hull");
          RDomain = mySim.getRegionManager().getRegion("03-Rotation Domain");
          Boundary bdy_BladeK = RDomain.getBoundaryManager().getBoundary("Propeller.Blade-Key");
          Boundary bdy_Blade = RDomain.getBoundaryManager().getBoundary("Propeller.Blade");
          Boundary bdy_Hub = RDomain.getBoundaryManager().getBoundary("Propeller.Hub");
          // Section Plane
          MidSection = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain, ODomain, RDomain}), new DoubleVector(new double[]{1.0, 0.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          MidSection.setPresentationName("Mid Section");
          MidSection.getOriginCoordinate().setDefinition("[0.5*${Lm}, 0.0, 0.0]");
          WaterPlane = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain, ODomain, RDomain}), new DoubleVector(new double[]{0.0, 0.0, 1.0}), new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          WaterPlane.setPresentationName("Water Plane");
          LongitudinalSection = (PlaneSection) mySim.getPartManager().createImplicitPart(new NeoObjectVector(new Object[]{SDomain, ODomain, RDomain}), new DoubleVector(new double[]{0.0, 1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.001, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
          LongitudinalSection.setPresentationName("Longitudinal Section");
          partDisplayer_2.getInputParts().setObjects(bdy_Hull, bdy_BladeK, bdy_Blade, bdy_Hub, MidSection, WaterPlane, LongitudinalSection);
        }
        mySim.getPartManager().getGroupsManager().createGroup("New Group");
        ((ClientServerObjectGroup) mySim.getPartManager().getGroupsManager().getObject("New Group")).setPresentationName("Mesh");
        ((ClientServerObjectGroup) mySim.getPartManager().getGroupsManager().getObject("Mesh")).getGroupsManager().groupObjects("Mesh", new NeoObjectVector(new Object[]{MidSection, WaterPlane, LongitudinalSection}), true);
        scene_2.resetCamera();
        CurrentView currentView_2 = scene_2.getCurrentView();
        ViewAngle viewAngle_2 = currentView_2.getViewAngle();
        viewAngle_2.setValue(15.0);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The mesh has been generated");
    }
  }

  public static class CreateReport extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create report program");
      try {
        mySim.println("@Try running create report program");
        // DragTotal Report
        ForceReport DragTotalReport = mySim.getReportManager().createReport(ForceReport.class);
        DragTotalReport.setPresentationName("DragTotal");
        DragTotalReport.getDirection().setComponents(-1.0, 0.0, 0.0);
        ReportMonitor DragTotalMonitor = DragTotalReport.createMonitor();
        mySim.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[]{DragTotalMonitor}), "DragTotal");
        mySim.getAnnotationManager().createReportAnnotation(DragTotalReport);
        // DragPressure Report
        ForceReport DragPressureReport = mySim.getReportManager().createReport(ForceReport.class);
        DragPressureReport.setPresentationName("DragPressure");
        DragPressureReport.getDirection().setComponents(-1.0, 0.0, 0.0);
        DragPressureReport.getForceOption().setSelected(ForceReportForceOption.Type.PRESSURE);
        DragPressureReport.createMonitor();
        // DragShear Report
        ForceReport DragShearReport = mySim.getReportManager().createReport(ForceReport.class);
        DragShearReport.setPresentationName("DragShear");
        DragShearReport.getDirection().setComponents(-1.0, 0.0, 0.0);
        DragShearReport.getForceOption().setSelected(ForceReportForceOption.Type.SHEAR);
        DragShearReport.createMonitor();
        // Rm Report
        StatisticsReport RmReport = mySim.getReportManager().createReport(StatisticsReport.class);
        RmReport.setPresentationName("Rm");
        RmReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        RmReport.setMonitor(DragTotalMonitor);
        LastNSamplesFilter lastNSamplesFilter_Rm = ((LastNSamplesFilter) RmReport.getSampleFilterManager().getObject("Last N Samples"));
        lastNSamplesFilter_Rm.setNSamples(200);
        RmReport.createMonitor();
        // DeltaRm Report
        ExpressionReport DeltaRmReport = mySim.getReportManager().createReport(ExpressionReport.class);
        DeltaRmReport.setPresentationName("DeltaRm");
        DeltaRmReport.setDefinition("abs(${Rm}-${DragTotal})/${Rm}*100");
        DeltaRmReport.createMonitor();
        // Rt Report
        ExpressionReport RtReport = mySim.getReportManager().createReport(ExpressionReport.class);
        RtReport.setPresentationName("Rt");
        RtReport.setDefinition("2*$Rm");
        RtReport.createMonitor();
        // Pd Report
        ExpressionReport PdReport = mySim.getReportManager().createReport(ExpressionReport.class);
        PdReport.setPresentationName("Pd");
        PdReport.setDefinition("$Rt*$Vm");
        PdReport.createMonitor();
        //Wall Y Plus Report
        Units units_none = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        PrimitiveFieldFunction volumefractionwater = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("VolumeFractionwater"));
        ThresholdPart wettedsurface = mySim.getPartManager().createThresholdPart(new NeoObjectVector(new Object[]{}), new DoubleVector(new double[]{0.5, 1.0}), units_none, volumefractionwater, 0);
        wettedsurface.setPresentationName("Wetted Surface");
        PrimitiveFieldFunction wallyplus = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("WallYplus"));
        AreaAverageReport WallYPlusReport = mySim.getReportManager().createReport(AreaAverageReport.class);
        WallYPlusReport.setPresentationName("Wall Y+");
        WallYPlusReport.setFieldFunction(wallyplus);
        WallYPlusReport.getParts().setObjects(wettedsurface);
        ReportMonitor WallYPlusMonitor = WallYPlusReport.createMonitor();
        mySim.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[]{WallYPlusMonitor}), "Wall Y+");
        // Create Sm_Calc Report
        UserFieldFunction One = mySim.getFieldFunctionManager().createFieldFunction();
        One.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
        One.setPresentationName("One");
        One.setFunctionName("One");
        One.setDefinition("1");
        SurfaceIntegralReport Sm_CalcReport = mySim.getReportManager().createReport(SurfaceIntegralReport.class);
        Sm_CalcReport.getParts().setObjects(wettedsurface);
        Sm_CalcReport.setFieldFunction(One);
        Sm_CalcReport.setPresentationName("Sm_Calc");
        Sm_CalcReport.createMonitor();
        // Create Ctm Report
        ExpressionReport CtmReport = mySim.getReportManager().createReport(ExpressionReport.class);
        CtmReport.setPresentationName("Ct");
        CtmReport.setDefinition("$Rm/(0.5*$rho*$Sm_Calc*pow($Vm, 2))");
        ReportMonitor CtmMonitor = CtmReport.createMonitor();
        mySim.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[]{CtmMonitor}), "Ctm");
        mySim.getAnnotationManager().createReportAnnotation(CtmReport);
        // Create Cf Report
        ExpressionReport CfReport = mySim.getReportManager().createReport(ExpressionReport.class);
        CfReport.setPresentationName("Cf");
        CfReport.setDefinition("0.075/pow(log(${Lm}*${Vm}/${mu}*${rho})-2, 2)");
        CfReport.createMonitor();
        // Create < SHIP > Report Group
        mySim.getReportManager().getGroupsManager().createGroup("New Group");
        ((ClientServerObjectGroup) mySim.getReportManager().getGroupsManager().getObject("New Group")).setPresentationName("SHIP");
        ((ClientServerObjectGroup) mySim.getReportManager().getGroupsManager().getObject("SHIP")).getGroupsManager().groupObjects("SHIP", new NeoObjectVector(new Object[] {DragTotalReport, DragPressureReport, DragShearReport, RmReport, DeltaRmReport, RtReport, PdReport, WallYPlusReport, Sm_CalcReport, CtmReport, CfReport}), true);
        if (!Overset) {
          Region StaticDomain = mySim.getRegionManager().getRegion("01-Static Domain");
          Boundary bdy_Hull = StaticDomain.getBoundaryManager().getBoundary("Ship.Hull");
          DragTotalReport.getParts().setObjects(bdy_Hull);
          DragPressureReport.getParts().setObjects(bdy_Hull);
          DragShearReport.getParts().setObjects(bdy_Hull);
          wettedsurface.getInputParts().setObjects(bdy_Hull);
        } else {
          Region OversetDomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          Boundary bdy_Hull = OversetDomain.getBoundaryManager().getBoundary("Ship.Hull");
          DragTotalReport.getParts().setObjects(bdy_Hull);
          DragPressureReport.getParts().setObjects(bdy_Hull);
          DragShearReport.getParts().setObjects(bdy_Hull);
          wettedsurface.getInputParts().setObjects(bdy_Hull);
          if (Propeller) {
            mySim.println("0");

          }
        }
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The report creation completed");
    }
  }

  public static class CreateScene extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      mySim.println("Note: This is create scene program");
      Region SDomain, ODomain;
      Boundary bdy_Hull, bdy_Sym, bdy_OvSym;
      try {
        mySim.println("@Try running create scene program");
        // Create Isosurface - Free Surface
        PrimitiveFieldFunction volumefractionwater = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("VolumeFractionwater"));
        IsoPart FreeSurface = mySim.getPartManager().createIsoPart(new NeoObjectVector(new Object[]{}), volumefractionwater);
        FreeSurface.setMode(IsoMode.ISOVALUE_SINGLE);
        SingleIsoValue singleIsoValue_0 = FreeSurface.getSingleIsoValue();
        singleIsoValue_0.getValueQuantity().setValue(0.5);
        FreeSurface.setPresentationName("Free Surface");
        PrimitiveFieldFunction Position = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("Position"));
        VectorComponentFieldFunction PositionZ = ((VectorComponentFieldFunction) Position.getComponentFunction(2));
        SymmetricRepeat symmetricRepeat_0 = ((SymmetricRepeat) mySim.getTransformManager().getObject("Far Field.Symmetry 1"));
        PartRepresentation partRepresentation_0 = ((PartRepresentation) mySim.getRepresentationManager().getObject("Geometry"));
        LogoAnnotation Annotation_logo = ((LogoAnnotation) mySim.getAnnotationManager().getObject("Logo"));
        // Create < 03-Wave Elevation >
        mySim.getSceneManager().createEmptyScene("Scene");
        Scene scene_3 = mySim.getSceneManager().getScene("Scene 1");
        scene_3.initializeAndWait();
        scene_3.resetCamera();
        scene_3.setPresentationName("03-Wave Elevation");
        PartDisplayer partDisplayer_3 = scene_3.getDisplayerManager().createPartDisplayer("Geometry", -1, 4);
        partDisplayer_3.initialize();
        partDisplayer_3.setOutline(false);
        partDisplayer_3.setSurface(true);
        partDisplayer_3.setVisTransform(symmetricRepeat_0);
        partDisplayer_3.setRepresentation(partRepresentation_0);
        //partDisplayer_3.getInputParts().setObjects(bdy_Hull);
        ScalarDisplayer scalarDisplayer_3 = scene_3.getDisplayerManager().createScalarDisplayer("Scalar");
        scalarDisplayer_3.initialize();
        scalarDisplayer_3.setFillMode(ScalarFillMode.NODE_FILLED);
        scalarDisplayer_3.setVisTransform(symmetricRepeat_0);
        scalarDisplayer_3.getInputParts().setObjects(FreeSurface);
        scalarDisplayer_3.getScalarDisplayQuantity().setFieldFunction(PositionZ);
        Legend legend_3 = scalarDisplayer_3.getLegend();
        legend_3.setLevels(64);
        legend_3.updateLayout(new DoubleVector(new double[]{0.85, 0.06}), 0.015, 0.80, 1);
        legend_3.setTitleHeight(0.030);
        legend_3.setLabelHeight(0.025);
        legend_3.setLabelFormat("%-#2.2f");
        legend_3.setNumberOfLabels(6);
        legend_3.setFontString("Times New Roman-Plain");
        legend_3.setShadow(false);
        scene_3.setViewOrientation(new DoubleVector(new double[]{0.0, 0.0, 1.0}), new DoubleVector(new double[]{0.0, 1.0, 0.0}));
        //scene_3.resetCamera();
        scene_3.setAxesViewport(new DoubleVector(new double[]{0.0, 0.0, 0.50, 0.2}));
        CurrentView currentView_3 = scene_3.getCurrentView();
        ViewAngle viewAngle_3 = currentView_3.getViewAngle();
        //viewAngle_3.setValue(110.0);
        // Annotation - Ct
        ReportAnnotation CtmAnnotation = ((ReportAnnotation) mySim.getAnnotationManager().getObject("Ct"));
        // Annotation - logo
        scene_3.getAnnotationPropManager().removePropsForAnnotations(Annotation_logo);
        // Annotation - report // 0.22, 0.85, 0.0 / 0.24, 0.02, 0.0 /
        FixedAspectAnnotationProp fixedAspectAnnotationProp_0 = (FixedAspectAnnotationProp) scene_3.getAnnotationPropManager().createPropForAnnotation(CtmAnnotation);
        fixedAspectAnnotationProp_0.setWidthStretchFactor(1.0);
        fixedAspectAnnotationProp_0.setHeight(0.05);
        fixedAspectAnnotationProp_0.setPosition(new DoubleVector(new double[]{0.22, 0.85, 0.0}));
        fixedAspectAnnotationProp_0.setVisible(true);
        // Annotation - Solution Time
        PhysicalTimeAnnotation physicalTimeAnnotation_0 = ((PhysicalTimeAnnotation) mySim.getAnnotationManager().getObject("Solution Time"));
        PhysicalTimeAnnotationProp physicalTimeAnnotationProp_0 = (PhysicalTimeAnnotationProp) scene_3.getAnnotationPropManager().createPropForAnnotation(physicalTimeAnnotation_0);
        physicalTimeAnnotationProp_0.setWidthStretchFactor(1.0);
        physicalTimeAnnotationProp_0.setHeight(0.05);
        physicalTimeAnnotationProp_0.setPosition(new DoubleVector(new double[]{0.22, 0.92, 0.0}));
        physicalTimeAnnotationProp_0.setVisible(true);
        // Create < 04-VOF Sym >
        mySim.getSceneManager().createEmptyScene("Scene");
        Scene scene_4 = mySim.getSceneManager().getScene("Scene 1");
        scene_4.initializeAndWait();
        scene_4.resetCamera();
        scene_4.setPresentationName("04-VOF Sym");
        ScalarDisplayer scalarDisplayer_4 = scene_4.getDisplayerManager().createScalarDisplayer("Scalar");
        scalarDisplayer_4.initialize();
        scalarDisplayer_4.setFillMode(ScalarFillMode.NODE_FILLED);
        scalarDisplayer_4.setVisTransform(symmetricRepeat_0);
        //scalarDisplayer_4.getInputParts().setObjects(bdy_Hull, bdy_Sym);
        scalarDisplayer_4.getScalarDisplayQuantity().setFieldFunction(volumefractionwater);
        Legend legend_4 = scalarDisplayer_4.getLegend();
        legend_4.updateLayout(new DoubleVector(new double[]{0.25, 0.05}), 0.5, 0.04, 0);
        legend_4.setLabelFormat("%-#2.2f");
        legend_4.setNumberOfLabels(6);
        legend_4.setFontString("Times New Roman-Plain");
        legend_4.setShadow(false);
        scene_4.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
        scene_4.getAnnotationPropManager().removePropsForAnnotations(Annotation_logo);
        PrimitiveFieldFunction wallyplus = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("WallYplus"));
        UserFieldFunction PdynamicFn = mySim.getFieldFunctionManager().createFieldFunction();
        PdynamicFn.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
        PdynamicFn.setPresentationName("Pdynamic");
        PdynamicFn.setFunctionName("Pdynamic");
        PdynamicFn.setDefinition("${Pressure}+${Density}*9.81*$${Centroid}[2]");
        PdynamicFn.setDimensions(Dimensions.Builder().force(1).build());
        if (!Overset) {
          SDomain = mySim.getRegionManager().getRegion("01-Static Domain");
          bdy_Hull = SDomain.getBoundaryManager().getBoundary("Ship.Hull");
          bdy_Sym = SDomain.getBoundaryManager().getBoundary("Far Field.Symmetry");
          FreeSurface.getInputParts().setObjects(SDomain);
          partDisplayer_3.getInputParts().setObjects(bdy_Hull);
          scene_3.resetCamera();
          viewAngle_3.setValue(110.0);
          scalarDisplayer_4.getInputParts().setObjects(bdy_Hull, bdy_Sym);
          scene_4.resetCamera();
        } else {
          SDomain = mySim.getRegionManager().getRegion("01-Static Domain");
          bdy_Sym = SDomain.getBoundaryManager().getBoundary("Far Field.Symmetry");
          ODomain = mySim.getRegionManager().getRegion("02-Overset Domain");
          bdy_Hull = ODomain.getBoundaryManager().getBoundary("Ship.Hull");
          bdy_OvSym = ODomain.getBoundaryManager().getBoundary("Overlap Field.Symmetry");
          FreeSurface.getInputParts().setObjects(SDomain, ODomain);
          partDisplayer_3.getInputParts().setObjects(bdy_Hull);
          scene_3.resetCamera();
          viewAngle_3.setValue(110.0);
          scalarDisplayer_4.getInputParts().setObjects(bdy_Hull, bdy_Sym, bdy_OvSym);
          scene_4.resetCamera();
        }
        createSceneXZ("05-VOF Hull", bdy_Hull, volumefractionwater);
        createSceneXZ("06-Wall Y+", bdy_Hull, wallyplus);
        createSceneXZ("07-Pdynamic", bdy_Hull, PdynamicFn);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.toString());
      }
      mySim.println("Note: The scene creation completed");
    }

    private void createSceneXZ(String name, Boundary partName, FieldFunction fieldName) {
      Simulation mySim = getActiveSimulation();
      SymmetricRepeat symmetricRepeat_0 = ((SymmetricRepeat) mySim.getTransformManager().getObject("Far Field.Symmetry 1"));
      LogoAnnotation Annotation_logo = ((LogoAnnotation) mySim.getAnnotationManager().getObject("Logo"));
      mySim.getSceneManager().createEmptyScene("Scene");
      Scene scene_8 = mySim.getSceneManager().getScene("Scene 1");
      scene_8.initializeAndWait();
      scene_8.resetCamera();
      scene_8.setPresentationName(name);
      ScalarDisplayer scalarDisplayer_8 = scene_8.getDisplayerManager().createScalarDisplayer("Scalar");
      scalarDisplayer_8.initialize();
      scalarDisplayer_8.setFillMode(ScalarFillMode.NODE_FILLED);
      scalarDisplayer_8.setVisTransform(symmetricRepeat_0);
      scalarDisplayer_8.getInputParts().setObjects(partName);
      scalarDisplayer_8.getScalarDisplayQuantity().setFieldFunction(fieldName);
      Legend legend_8 = scalarDisplayer_8.getLegend();
      legend_8.updateLayout(new DoubleVector(new double[]{0.25, 0.3}), 0.5, 0.04, 0);
      legend_8.setLabelFormat("%-#3.3f");
      legend_8.setNumberOfLabels(6);
      legend_8.setFontString("Times New Roman-Plain");
      legend_8.setShadow(false);
      scene_8.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
      scene_8.resetCamera();
      scene_8.getAnnotationPropManager().removePropsForAnnotations(Annotation_logo);
    }
  }

  public static class WaveCut extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      IsoPart FreeSurface = ((IsoPart) mySim.getPartManager().getObject("Free Surface"));
      PrimitiveFieldFunction Position = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("Position"));
      VectorComponentFieldFunction PositionZ = ((VectorComponentFieldFunction) Position.getComponentFunction(2));
      UserFieldFunction x_Lpp = mySim.getFieldFunctionManager().createFieldFunction();
      x_Lpp.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
      x_Lpp.setPresentationName("x/Lpp");
      x_Lpp.setFunctionName("x/Lpp");
      x_Lpp.setDefinition("$${Position}[0]/${Lm}");
      // WaveCut
      PlaneSection WaveCut_1 = (PlaneSection) mySim.getPartManager().createImplicitPart(
          new NeoObjectVector(new Object[]{}), new DoubleVector(new double[]{0.0, 1.0, 0.0}),
          new DoubleVector(new double[]{0.0, 1.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
      WaveCut_1.setPresentationName("y/Lpp = 0.12");
      WaveCut_1.getOriginCoordinate().setDefinition("[0.0, 0.12*${Lm}, 0.0]");
      WaveCut_1.getInputParts().setObjects(FreeSurface);
      PlaneSection WaveCut_2 = (PlaneSection) mySim.getPartManager().createImplicitPart(
          new NeoObjectVector(new Object[]{}), new DoubleVector(new double[]{0.0, 1.0, 0.0}),
          new DoubleVector(new double[]{0.0, 1.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
      WaveCut_2.setPresentationName("y/Lpp = 0.15");
      WaveCut_2.getOriginCoordinate().setDefinition("[0.0, 0.15*${Lm}, 0.0]");
      WaveCut_2.getInputParts().setObjects(FreeSurface);
      PlaneSection WaveCut_3 = (PlaneSection) mySim.getPartManager().createImplicitPart(
          new NeoObjectVector(new Object[]{}), new DoubleVector(new double[]{0.0, 1.0, 0.0}),
          new DoubleVector(new double[]{0.0, 1.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
      WaveCut_3.setPresentationName("y/Lpp = 0.20");
      WaveCut_3.getOriginCoordinate().setDefinition("[0.0, 0.20*${Lm}, 0.0]");
      WaveCut_3.getInputParts().setObjects(FreeSurface);
      PlaneSection WaveCut_4 = (PlaneSection) mySim.getPartManager().createImplicitPart(
          new NeoObjectVector(new Object[]{}), new DoubleVector(new double[]{0.0, 1.0, 0.0}),
          new DoubleVector(new double[]{0.0, 1.0, 0.0}), 0, 1, new DoubleVector(new double[]{0.0}));
      WaveCut_4.setPresentationName("y/Lpp = 0.30");
      WaveCut_4.getOriginCoordinate().setDefinition("[0.0, 0.30*${Lm}, 0.0]");
      WaveCut_4.getInputParts().setObjects(FreeSurface);

      XYPlot xYPlot_0 = mySim.getPlotManager().createPlot(XYPlot.class);
      xYPlot_0.setPresentationName("Wave Cut");
      xYPlot_0.getParts().setObjects(WaveCut_1, WaveCut_2, WaveCut_3, WaveCut_4);
      AxisType axisType_0 = xYPlot_0.getXAxisType();
      axisType_0.setMode(AxisTypeMode.SCALAR);
      FieldFunctionUnits fieldFunctionUnits_0 = axisType_0.getScalarFunction();
      fieldFunctionUnits_0.setFieldFunction(x_Lpp);
      YAxisType yAxisType_0 = xYPlot_0.getYAxes().getAxisType("Y Type 1");
      yAxisType_0.setSmooth(true);
      FieldFunctionUnits fieldFunctionUnits_1 = yAxisType_0.getScalarFunction();
      fieldFunctionUnits_1.setFieldFunction(PositionZ);
      Cartesian2DAxisManager cartesian2DAxisManager_0 = ((Cartesian2DAxisManager) xYPlot_0.getAxisManager());
      Cartesian2DAxis cartesian2DAxis_y = ((Cartesian2DAxis) cartesian2DAxisManager_0.getAxis("Left Axis"));
      AxisTitle axisTitle_y = cartesian2DAxis_y.getTitle();
      axisTitle_y.setText("Wave Elevation (m)");
      Cartesian2DAxis cartesian2DAxis_x = ((Cartesian2DAxis) cartesian2DAxisManager_0.getAxis("Bottom Axis"));
      cartesian2DAxis_x.setMinimum(0.0);
      cartesian2DAxis_x.setMaximum(1.0);

      InternalDataSet internalDataSet_2 = ((InternalDataSet) yAxisType_0.getDataSetManager().getDataSet("y/Lpp = 0.12"));
      internalDataSet_2.setNeedsSorting(true);
      LineStyle lineStyle_2 = internalDataSet_2.getLineStyle();
      lineStyle_2.getLinePatternOption().setSelected(LinePatternOption.Type.SOLID);
      SymbolStyle symbolStyle_2 = internalDataSet_2.getSymbolStyle();
      symbolStyle_2.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.NONE);
      InternalDataSet internalDataSet_3 = ((InternalDataSet) yAxisType_0.getDataSetManager().getDataSet("y/Lpp = 0.15"));
      internalDataSet_3.setNeedsSorting(true);
      LineStyle lineStyle_3 = internalDataSet_3.getLineStyle();
      lineStyle_3.getLinePatternOption().setSelected(LinePatternOption.Type.SOLID);
      SymbolStyle symbolStyle_3 = internalDataSet_3.getSymbolStyle();
      symbolStyle_3.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.NONE);
      InternalDataSet internalDataSet_4 = ((InternalDataSet) yAxisType_0.getDataSetManager().getDataSet("y/Lpp = 0.20"));
      internalDataSet_4.setNeedsSorting(true);
      LineStyle lineStyle_4 = internalDataSet_4.getLineStyle();
      lineStyle_4.getLinePatternOption().setSelected(LinePatternOption.Type.SOLID);
      SymbolStyle symbolStyle_4 = internalDataSet_4.getSymbolStyle();
      symbolStyle_4.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.NONE);
      InternalDataSet internalDataSet_5 = ((InternalDataSet) yAxisType_0.getDataSetManager().getDataSet("y/Lpp = 0.30"));
      internalDataSet_5.setNeedsSorting(true);
      LineStyle lineStyle_5 = internalDataSet_5.getLineStyle();
      lineStyle_5.getLinePatternOption().setSelected(LinePatternOption.Type.SOLID);
      SymbolStyle symbolStyle_5 = internalDataSet_5.getSymbolStyle();
      symbolStyle_5.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.NONE);
    }
  }

  public static class SetupSolver extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      // Solver
      ImplicitUnsteadySolver Solver_0 = mySim.getSolverManager().getSolver(ImplicitUnsteadySolver.class);
      Solver_0.getTimeDiscretizationOption().setSelected(TimeDiscretizationOption.Type.FIRST_ORDER);
      Solver_0.getTimeStep().setDefinition("floor(${Lm}/${Vm}*1E0)*1E-2");
      InnerIterationStoppingCriterion innerIteration_0 = ((InnerIterationStoppingCriterion) mySim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Inner Iterations"));
      innerIteration_0.setMaximumNumberInnerIterations(5);
      PhysicalTimeStoppingCriterion physicalTime_0 = ((PhysicalTimeStoppingCriterion) mySim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Physical Time"));
      physicalTime_0.getMaximumTime().setDefinition("${TC}");
      // physicalTime_0.getMaximumTime().setDefinition("floor(${Lm}/${Vm}*4)");
      StepStoppingCriterion stepStoppingCriterion_0 = ((StepStoppingCriterion) mySim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps"));
      stepStoppingCriterion_0.setIsUsed(false);
    }
  }

  public static class SaveFile extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      Units units_s = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
      // AutoSave
      AutoSave autoSave_0 = mySim.getSimulationIterator().getAutoSave();
      autoSave_0.setMaxAutosavedFiles(0);
      autoSave_0.setFormatWidth(2);
      StarUpdate starUpdate_0 = autoSave_0.getStarUpdate();
      starUpdate_0.setEnabled(true);
      starUpdate_0.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.DELTATIME);
      DeltaTimeUpdateFrequency deltaTimeUpdateFrequency_0 = starUpdate_0.getDeltaTimeUpdateFrequency();
      deltaTimeUpdateFrequency_0.setDeltaTime("10.0", units_s);
      // Save
      Date d = new Date();
      DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmm");
      String s = sdf3.format(d);
      if (!Overset) {
        String work1FileName = "Resistance";
        File dir = new File(resolveWorkPath() + File.separator + work1FileName);
        if (dir.exists()) {
          mySim.println(dir.getAbsolutePath() + " already exists");
        } else {
          dir.mkdir();
          mySim.println(dir.getAbsolutePath() + " created successfully");
        }
        mySim.saveState(resolvePath(work1FileName + File.separator + "CASE_RESISTANCE_T=0.0m_Vs=0.0kn_wopro_" + s + ".sim"));
      } else if (!Propeller) {
        String work1FileName = "Resistance";
        File dir = new File(resolveWorkPath() + File.separator + work1FileName);
        if (dir.exists()) {
          mySim.println(dir.getAbsolutePath() + " already exists");
        } else {
          dir.mkdir();
          mySim.println(dir.getAbsolutePath() + " created successfully");
        }
        mySim.saveState(resolvePath(work1FileName + File.separator + "CASE_RESISTANCE_T=0.0m_Vs=0.0kn_wopro_overset_" + s + ".sim"));
      } else {
        String work1FileName = "Self-Propulsion";
        File dir = new File(resolveWorkPath() + File.separator + work1FileName);
        if (dir.exists()) {
          mySim.println(dir.getAbsolutePath() + " already exists");
        } else {
          dir.mkdir();
          mySim.println(dir.getAbsolutePath() + " created successfully");
        }
        mySim.saveState(resolvePath(work1FileName + File.separator + "CASE_SELFPROPULSION_T=0.0m_Vs=0.0kn_" + s + ".sim"));
      }
    }
  }
  @SuppressWarnings("SameParameterValue")
  public static class CircularSave extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      for (int i = 0; i < Param_Tm.length; i++){
        for (int j = 0; j < Param_Vs.length; j++) {
          int a = j + 1;
          ChangeParam(ParamName[1], Param_Tm[i]);
          ChangeParam(ParamName[2], Param_Vm[j]);
          ChangeParam(ParamName[3], Param_Fst[j]);
          mySim.println("i = " + a + ": Vm = " + Param_Vm[j] + ", Fst = " + Param_Fst[j]);
          SaveCase("case", Param_Tm[i], Param_Vs[j]);
        }
        if ( i+1 < Param_Tm.length ) {
          translateGeometry(Param_Tm[i+1]-Param_Tm[i]);
        }
      }
    }
    private void translateGeometry(double trans) {
      Simulation mySim = getActiveSimulation();
      Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
      MeshPart meshPart_0 = ((MeshPart) mySim.get(SimulationPartManager.class).getPart("Ship"));
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      mySim.get(SimulationPartManager.class).translateParts(new NeoObjectVector(new Object[] {meshPart_0}), new DoubleVector(new double[] {0.0, 0.0, -trans}), new NeoObjectVector(new Object[] {units_m, units_m, units_m}), GCS);
    }
    private void ChangeParam(String name, double param) {
      Simulation mySim = getActiveSimulation();
      ScalarGlobalParameter Parameter = ((ScalarGlobalParameter) mySim.get(GlobalParameterManager.class).getObject(name));
      Parameter.getQuantity().setValue(param);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void SaveCase(String caseName, double param1, double param2) {
      Simulation mySim = getActiveSimulation();
      if (!Overset) {
        String work1FileName = "Resistance";
        File dir = new File(resolveWorkPath() + File.separator + work1FileName);
        if (dir.exists()) {
          mySim.println(dir.getAbsolutePath() + " already exists");
        } else {
          dir.mkdir();
          mySim.println(dir.getAbsolutePath() + " created successfully");
        }
        mySim.saveState(resolvePath(work1FileName + File.separator + caseName + "_RESISTANCE_T="+param1+"m_Vs="+param2+"kn" + ".sim"));
      } else if (!Propeller) {
        String work1FileName = "Resistance";
        File dir = new File(resolveWorkPath() + File.separator + work1FileName);
        if (dir.exists()) {
          mySim.println(dir.getAbsolutePath() + " already exists");
        } else {
          dir.mkdir();
          mySim.println(dir.getAbsolutePath() + " created successfully");
        }
        mySim.saveState(resolvePath(work1FileName + File.separator + caseName + "_RESISTANCE_T="+param1+"m_Vs="+param2+"kn_overset" + ".sim"));
      } else {
        String work1FileName = "Self-Propulsion";
        File dir = new File(resolveWorkPath() + File.separator + work1FileName);
        if (dir.exists()) {
          mySim.println(dir.getAbsolutePath() + " already exists");
        } else {
          dir.mkdir();
          mySim.println(dir.getAbsolutePath() + " created successfully");
        }
        mySim.saveState(resolvePath(work1FileName + File.separator + caseName + "_SELFPROPULSION_T="+param1+"m_Vs="+param2+"kn" + ".sim"));
      }
    }
  }

}