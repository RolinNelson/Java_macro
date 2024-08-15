package macro;

import java.io.*;
import java.text.*;
import java.util.*;

import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.flow.*;
import star.keturb.*;
import star.kwturb.*;
import star.material.*;
import star.meshing.*;
import star.metrics.*;
import star.motion.*;
import star.prismmesher.*;
import star.segregatedflow.*;
import star.trimmer.*;
import star.turbulence.*;
import star.vis.*;
import star.walldistance.*;

@SuppressWarnings("unused")
public class Propeller_03_BasicSetting extends StarMacro {

  private static boolean DES;

  static String caseName = "P4119";
  static Double[] Param_J = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};

  public void execute() {
    SetSwitch(true);
    play(new CreateDomain());
    play(new GeometryScene());
    play(new PhysicsField());
    play(new BoundaryCondition());
    play(new MeshContinuum());
    play(new GenerateMesh());
    play(new CreateReport());
    play(new CreateScene());
    play(new SetupSolver());
//    play(new SaveFile());
    play(new CircularSave());
  }

  public static void SetSwitch(boolean des) {
    DES = des;
  }
  @SuppressWarnings("unchecked")
  public static class CreateDomain extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
      CartesianCoordinateSystem LCS_HP = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
      // Definition Propeller Surfaces
      MeshPart Part_Prop = ((MeshPart) mySim.get(SimulationPartManager.class).getPart("Propeller"));
      // Create Far Field
      MeshPartFactory meshPartFactory_0 = mySim.get(MeshPartFactory.class);
      SimpleBlockPart Part_FarField = meshPartFactory_0.createNewBlockPart(mySim.get(SimulationPartManager.class));
      Part_FarField.setCoordinateSystem(GCS);
      Part_FarField.getCorner1().setCoordinateSystem(GCS);
      Part_FarField.getCorner1().setDefinition("[-5*${Dp}, -5*${Dp}, -5*${Dp}]");
      Part_FarField.getCorner2().setCoordinateSystem(GCS);
      Part_FarField.getCorner2().setDefinition("[10*${Dp}, 5*${Dp},5*${Dp}]");
      Part_FarField.rebuildSimpleShapePart();
      Part_FarField.setPresentationName("Far Field");
      PartSurface FarField_In = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface");
      Part_FarField.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(new Object[]{FarField_In}), 89.0);
      FarField_In.setPresentationName("Inlet");
      PartSurface FarField_Out = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 6");
      FarField_Out.setPresentationName("Outlet");
      PartSurface FarField_Far = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 2");
      PartSurface FarField_Far02 = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 3");
      PartSurface FarField_Far03 = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 4");
      PartSurface FarField_Far04 = Part_FarField.getPartSurfaceManager().getPartSurface("Block Surface 5");
      Part_FarField.combinePartSurfaces(new NeoObjectVector(new Object[]{FarField_Far, FarField_Far02, FarField_Far03, FarField_Far04}));
      FarField_Far.setPresentationName("Far");
      // Create Rotation Field
      final double L1 = -0.15;
      final double L2 = 0.13;
      SimpleCylinderPart Part_RotationField = meshPartFactory_0.createNewCylinderPart(mySim.get(SimulationPartManager.class));
      Part_RotationField.setDoNotRetessellate(true);
      Part_RotationField.setCoordinateSystem(LCS_HP);
      Part_RotationField.getStartCoordinate().setCoordinateSystem(LCS_HP);
      Part_RotationField.getStartCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{L1, 0.0, 0.0}));
      Part_RotationField.getEndCoordinate().setCoordinateSystem(LCS_HP);
      Part_RotationField.getEndCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{L2, 0.0, 0.0}));
      Part_RotationField.getRadius().setUnits(units_m);
      Part_RotationField.getRadius().setDefinition("${Dp}*0.6");
      Part_RotationField.getTessellationDensityOption().setSelected(TessellationDensityOption.Type.MEDIUM);
      Part_RotationField.rebuildSimpleShapePart();
      Part_RotationField.setDoNotRetessellate(false);
      Part_RotationField.setPresentationName("Rotation Field");
      PartSurface RotationField_Interface = Part_RotationField.getPartSurfaceManager().getPartSurface("Cylinder Surface");
      RotationField_Interface.setPresentationName("Interface");
      // 01-Static Domain
      SubtractPartsOperation Operation_StaticDomain = (SubtractPartsOperation) mySim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{Part_FarField, Part_RotationField, Part_Prop}));
      Operation_StaticDomain.getTargetPartManager().setQuery(null);
      Operation_StaticDomain.getTargetPartManager().setObjects(Part_FarField);
      Operation_StaticDomain.setPerformCADBoolean(false);
      Operation_StaticDomain.execute();
      Operation_StaticDomain.setPresentationName("01-Static Domain");
      MeshOperationPart Part_StaticDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Subtract"));
      Part_StaticDomain.setPresentationName("01-Static Domain");
      // 02-Rotation Domain
      SubtractPartsOperation Operation_RotationDomain = (SubtractPartsOperation) mySim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{Part_Prop, Part_RotationField}));
      Operation_RotationDomain.getTargetPartManager().setQuery(null);
      Operation_RotationDomain.getTargetPartManager().setObjects(Part_RotationField);
      Operation_RotationDomain.setPerformCADBoolean(false);
      Operation_RotationDomain.execute();
      Operation_RotationDomain.setPresentationName("02-Rotation Domain");
      MeshOperationPart Part_RotationDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Subtract"));
      Part_RotationDomain.setPresentationName("02-Rotation Domain");
      mySim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{Part_StaticDomain, Part_RotationDomain}), "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null, "OneFeatureCurvePerPartCurve", null, RegionManager.CreateInterfaceMode.NONE);
    }
  }

  public static class GeometryScene extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      // Definition Domain Face
      MeshOperationPart Part_RotationDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("02-Rotation Domain"));
      PartSurface RoDomain_BladeK = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade-Key");
      PartSurface RoDomain_Blade = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade");
      PartSurface RoDomain_Hub = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Propeller.Hub");
      // Create 01-Geometry Scene
      mySim.getSceneManager().createEmptyScene("Scene");
      Scene scene_1 = mySim.getSceneManager().getScene("Scene 1");
      scene_1.initializeAndWait();
      scene_1.resetCamera();
      scene_1.setPresentationName("01-Geometry");
      PartDisplayer partDisplayer_1 = scene_1.getDisplayerManager().createPartDisplayer("Geometry", -1, 4);
      partDisplayer_1.initialize();
      partDisplayer_1.setSurface(true);
      partDisplayer_1.setOutline(false);
      partDisplayer_1.setColorMode(PartColorMode.DP);
      partDisplayer_1.getInputParts().setObjects(RoDomain_BladeK, RoDomain_Blade, RoDomain_Hub);
      scene_1.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
      scene_1.resetCamera();
      CurrentView currentView_1 = scene_1.getCurrentView();
      ViewAngle viewAngle_1 = currentView_1.getViewAngle();
      viewAngle_1.setValue(30.0);
    }
  }

  public static class PhysicsField extends StarMacro {
    @Override
    public void execute() {
      // Create Physics Continuum
      Simulation mySim = getActiveSimulation();
      PhysicsContinuum PhysicsField = mySim.getContinuumManager().createContinuum(PhysicsContinuum.class);
      PhysicsField.setPresentationName("Physics Field");
      PhysicsField.enable(ThreeDimensionalModel.class);
      PhysicsField.enable(ImplicitUnsteadyModel.class);
      PhysicsField.enable(SingleComponentLiquidModel.class);
      PhysicsField.enable(SegregatedFlowModel.class);
      PhysicsField.enable(ConstantDensityModel.class);
      PhysicsField.enable(TurbulentModel.class);
      // PhysicsField.enable(GravityModel.class);
      PhysicsField.enable(CellQualityRemediationModel.class);
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
      mySim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
      ScalarGlobalParameter Parameter = ((ScalarGlobalParameter) mySim.get(GlobalParameterManager.class).getObject("Scalar"));
      Parameter.setPresentationName("Va");
      Parameter.getQuantity().setDefinition("${J}*${n}*${Dp}");
      VelocityProfile velocityProfile_0 = PhysicsField.getInitialConditions().get(VelocityProfile.class);
      velocityProfile_0.getMethod(ConstantVectorProfileMethod.class).getQuantity().setDefinition("[${VA}, 0.0, 0.0]");
    }
  }

  public static class BoundaryCondition extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      // Definition Boundary
      Region StaticDomain = mySim.getRegionManager().getRegion("01-Static Domain");
      Boundary bdy_In = StaticDomain.getBoundaryManager().getBoundary("Far Field.Inlet");
      Boundary bdy_Out = StaticDomain.getBoundaryManager().getBoundary("Far Field.Outlet");
      Boundary bdy_Far = StaticDomain.getBoundaryManager().getBoundary("Far Field.Far");
      Boundary bdy_InterfaceS = StaticDomain.getBoundaryManager().getBoundary("Rotation Field.Interface");
      Region RotationDomain = mySim.getRegionManager().getRegion("02-Rotation Domain");
      Boundary bdy_InterfaceR = RotationDomain.getBoundaryManager().getBoundary("Rotation Field.Interface");
      SymmetryBoundary symmetryBdy = mySim.get(ConditionTypeManager.class).get(SymmetryBoundary.class);
      bdy_Far.setBoundaryType(symmetryBdy);
      InletBoundary inletBdy = mySim.get(ConditionTypeManager.class).get(InletBoundary.class);
      bdy_In.setBoundaryType(inletBdy);
      VelocityMagnitudeProfile velocityMagnitudeProfile_0 = bdy_In.getValues().get(VelocityMagnitudeProfile.class);
      velocityMagnitudeProfile_0.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${VA}");
      PressureBoundary pressureBdy = mySim.get(ConditionTypeManager.class).get(PressureBoundary.class);
      bdy_Out.setBoundaryType(pressureBdy);
      // Setup Interface
      BoundaryInterface boundaryInterface_0 = mySim.getInterfaceManager().createBoundaryInterface(bdy_InterfaceR, bdy_InterfaceS, "Interface");
      boundaryInterface_0.setPresentationName("Interface");
      // Motion
      UserFieldFunction PI = mySim.getFieldFunctionManager().createFieldFunction();
      PI.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
      PI.setPresentationName("PI");
      PI.setFunctionName("PI");
      PI.setDefinition("4*atan(1)");
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
      CartesianCoordinateSystem LCS_HP = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
      //CartesianCoordinateSystem LCS_P = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Propeller"));
      RotatingMotion rotatingMotion_0 = mySim.get(MotionManager.class).createMotion(RotatingMotion.class, "Rotation");
      rotatingMotion_0.setCoordinateSystem(LCS_HP);
      rotatingMotion_0.getAxisDirection().setComponents(-1.0, 0.0, 0.0);
      RotationRate rotationRate_0 = ((RotationRate) rotatingMotion_0.getRotationSpecification());
      rotationRate_0.getRotationRate().setDefinition("${n}*2*${PI}");
      MotionSpecification motionSpecification_0 = RotationDomain.getValues().get(MotionSpecification.class);
      motionSpecification_0.setMotion(rotatingMotion_0);
    }
  }
  @SuppressWarnings("unchecked")
  public static class MeshContinuum extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      Units units_m = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
      CartesianCoordinateSystem LCS_HP = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
      MeshOperationPart Part_StaticDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("01-Static Domain"));
      MeshOperationPart Part_RotationDomain = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("02-Rotation Domain"));
      MeshPartFactory meshPartFactory_0 = mySim.get(MeshPartFactory.class);
      // Propeller Refine - Vortex
      mySim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
      ScalarGlobalParameter Parameter = ((ScalarGlobalParameter) mySim.get(GlobalParameterManager.class).getObject("Scalar"));
      Parameter.setPresentationName("l");
      Parameter.getQuantity().setValue(5.0);
      SimpleCylinderPart Part_Inside = meshPartFactory_0.createNewCylinderPart(mySim.get(SimulationPartManager.class));
      Part_Inside.setCoordinateSystem(LCS_HP);
      Part_Inside.getStartCoordinate().setCoordinateSystem(LCS_HP);
      Part_Inside.getStartCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{0.0, 0.0, 0.0}));
      Part_Inside.getEndCoordinate().setCoordinateSystem(LCS_HP);
      Part_Inside.getEndCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{0.1, 0.0, 0.0}));
      Part_Inside.getEndCoordinate().setDefinition("[${Dp}*${l}, 0.0, 0.0]");
      Part_Inside.getRadius().setUnits(units_m);
      Part_Inside.getRadius().setDefinition("${Dp}/2*0.80");
      Part_Inside.getTessellationDensityOption().setSelected(TessellationDensityOption.Type.MEDIUM);
      Part_Inside.rebuildSimpleShapePart();
      Part_Inside.setPresentationName("Vortex-Inside");
      SimpleCylinderPart Part_Outside = meshPartFactory_0.createNewCylinderPart(mySim.get(SimulationPartManager.class));
      Part_Outside.setCoordinateSystem(LCS_HP);
      Part_Outside.getStartCoordinate().setCoordinateSystem(LCS_HP);
      Part_Outside.getStartCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{0.0, 0.0, 0.0}));
      Part_Outside.getEndCoordinate().setCoordinateSystem(LCS_HP);
      Part_Outside.getEndCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{0.1, 0.0, 0.0}));
      Part_Outside.getEndCoordinate().setDefinition("[${Dp}*${l}, 0.0, 0.0]");
      Part_Outside.getRadius().setUnits(units_m);
      Part_Outside.getRadius().setDefinition("${Dp}/2*1.10");
      Part_Outside.getTessellationDensityOption().setSelected(TessellationDensityOption.Type.MEDIUM);
      Part_Outside.rebuildSimpleShapePart();
      Part_Outside.setPresentationName("Vortex-Outside");
      SimpleCylinderPart Part_Hub = meshPartFactory_0.createNewCylinderPart(mySim.get(SimulationPartManager.class));
      Part_Hub.setCoordinateSystem(LCS_HP);
      Part_Hub.getStartCoordinate().setCoordinateSystem(LCS_HP);
      Part_Hub.getStartCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{0.0, 0.0, 0.0}));
      Part_Hub.getEndCoordinate().setCoordinateSystem(LCS_HP);
      Part_Hub.getEndCoordinate().setCoordinate(units_m, units_m, units_m, new DoubleVector(new double[]{0.1, 0.0, 0.0}));
      Part_Hub.getEndCoordinate().setDefinition("[${Dp}*${l}, 0.0, 0.0]");
      Part_Hub.getRadius().setUnits(units_m);
      Part_Hub.getRadius().setDefinition("${Dp}*0.11");
      Part_Hub.getTessellationDensityOption().setSelected(TessellationDensityOption.Type.MEDIUM);
      Part_Hub.rebuildSimpleShapePart();
      Part_Hub.setPresentationName("Vortex-Hub");
      // Create Propeller Refine
      SimpleCylinderPart Part_RotationField = ((SimpleCylinderPart) mySim.get(SimulationPartManager.class).getPart("Rotation Field"));
      SimpleCylinderPart Propeller_Refine = (SimpleCylinderPart) Part_RotationField.duplicatePart(mySim.get(SimulationPartManager.class));
      mySim.get(SimulationPartManager.class).scaleParts(new NeoObjectVector(new Object[]{Propeller_Refine}), new DoubleVector(new double[]{1.2, 1.2, 1.2}), LCS_HP);
      Propeller_Refine.setPresentationName("Propeller Refine");
      // Vortex-Refine
      SubtractPartsOperation Operation_Vortex = (SubtractPartsOperation) mySim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{Part_Inside, Part_Outside}));
      Operation_Vortex.getTargetPartManager().setQuery(null);
      Operation_Vortex.getTargetPartManager().setObjects(Part_Outside);
      Operation_Vortex.setPerformCADBoolean(false);
      Operation_Vortex.execute();
      Operation_Vortex.setPresentationName("Vortex-Refine");
      MeshOperationPart Part_Vortex = ((MeshOperationPart) mySim.get(SimulationPartManager.class).getPart("Subtract"));
      Part_Vortex.setPresentationName("Vortex-Refine");
      // Create Static Domain Mesh
      AutoMeshOperation StaticDomainMesh = mySim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.trimmer.TrimmerAutoMesher", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{Part_StaticDomain}));
      StaticDomainMesh.setPresentationName("01-Static Domain Mesh");
      StaticDomainMesh.getMesherParallelModeOption().setSelected(MesherParallelModeOption.Type.PARALLEL);
      TrimmerAutoMesher trimmerAutoMesher_S = ((TrimmerAutoMesher) StaticDomainMesh.getMeshers().getObject("Trimmed Cell Mesher"));
      trimmerAutoMesher_S.setDoMeshAlignment(true);
      MeshAlignmentLocation meshAlignmentLocation_S = StaticDomainMesh.getDefaultValues().get(MeshAlignmentLocation.class);
      meshAlignmentLocation_S.getLocation().setComponents(0.0, 0.0, 0.0);
      PrismAutoMesher prismAutoMesher_S = ((PrismAutoMesher) StaticDomainMesh.getMeshers().getObject("Prism Layer Mesher"));
      prismAutoMesher_S.setMinimumThickness(5.0);
      prismAutoMesher_S.setLayerChoppingPercentage(25.0);
      prismAutoMesher_S.setBoundaryMarchAngle(75.0);
      prismAutoMesher_S.setNearCoreLayerAspectRatio(0.6);
      StaticDomainMesh.getDefaultValues().get(BaseSize.class).setDefinition("floor(${Dp}/${GD}*1E3)/1E3");
      PartsSimpleTemplateGrowthRate partsSimpleTemplateGrowthRate_Static = StaticDomainMesh.getDefaultValues().get(PartsSimpleTemplateGrowthRate.class);
      partsSimpleTemplateGrowthRate_Static.getGrowthRateOption().setSelected(PartsGrowthRateOption.Type.VERYSLOW);
      SurfaceCurvature surfaceCurvature_1 = StaticDomainMesh.getDefaultValues().get(SurfaceCurvature.class);
      surfaceCurvature_1.setEnableCurvatureDeviationDist(true);
      surfaceCurvature_1.setNumPointsAroundCircle(72.0);
      surfaceCurvature_1.getCurvatureDeviationDistance().setValue(0.005);
      MaximumCellSize maximumCellSize_S = StaticDomainMesh.getDefaultValues().get(MaximumCellSize.class);
      maximumCellSize_S.getRelativeSizeScalar().setValue(1600.0);
      // 01-Static Domain : Propeller Refine
      VolumeCustomMeshControl Mesh_Refine = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
      Mesh_Refine.setPresentationName("Propeller Refine");
      Mesh_Refine.getGeometryObjects().setQuery(null);
      Mesh_Refine.getGeometryObjects().setObjects(Propeller_Refine);
      VolumeControlTrimmerSizeOption MeshOp_Refine = Mesh_Refine.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
      MeshOp_Refine.setVolumeControlBaseSizeOption(true);
      VolumeControlSize MeshS_Refine = Mesh_Refine.getCustomValues().get(VolumeControlSize.class);
      MeshS_Refine.getRelativeSizeScalar().setValue(100.0);
      // 01-Static Domain : Vortex
      VolumeCustomMeshControl Mesh_Vortex = StaticDomainMesh.getCustomMeshControls().createVolumeControl();
      Mesh_Vortex.setPresentationName("Vortex Refine");
      Mesh_Vortex.getGeometryObjects().setQuery(null);
      Mesh_Vortex.getGeometryObjects().setObjects(Part_Vortex, Part_Hub);
      Mesh_Vortex.setEnableControl(false);// ture or false
      VolumeControlTrimmerSizeOption MeshOp_Vortex = Mesh_Vortex.getCustomConditions().get(VolumeControlTrimmerSizeOption.class);
      MeshOp_Vortex.setVolumeControlBaseSizeOption(true);
      VolumeControlSize MeshS_Vortex = Mesh_Vortex.getCustomValues().get(VolumeControlSize.class);
      MeshS_Vortex.getRelativeSizeScalar().setValue(100.0);
      // 01-Static Domain : Far Field Surface Control
      PartSurface StaticDomain_In = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Inlet");
      PartSurface StaticDomain_Out = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Outlet");
      PartSurface StaticDomain_Far = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Far Field.Far");
      PartSurface StaticDomain_Inter = Part_StaticDomain.getPartSurfaceManager().getPartSurface("Rotation Field.Interface");
      SurfaceCustomMeshControl Mesh_Far = StaticDomainMesh.getCustomMeshControls().createSurfaceControl();
      Mesh_Far.setPresentationName("Boundary");
      Mesh_Far.getGeometryObjects().setQuery(null);
      Mesh_Far.getGeometryObjects().setObjects(StaticDomain_In, StaticDomain_Out, StaticDomain_Far);
      Mesh_Far.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
      Mesh_Far.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
      PartsCustomizePrismMesh MeshP_Far = Mesh_Far.getCustomConditions().get(PartsCustomizePrismMesh.class);
      MeshP_Far.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.DISABLE);
      PartsTargetSurfaceSize MeshT_Far = Mesh_Far.getCustomValues().get(PartsTargetSurfaceSize.class);
      MeshT_Far.getRelativeSizeScalar().setValue(1600.0);
      PartsMinimumSurfaceSize MeshM_Far = Mesh_Far.getCustomValues().get(PartsMinimumSurfaceSize.class);
      MeshM_Far.getRelativeSizeScalar().setValue(1600.0);
      // 01-Static Domain : Interface
      SurfaceCustomMeshControl Mesh_InterS = StaticDomainMesh.getCustomMeshControls().createSurfaceControl();
      Mesh_InterS.setPresentationName("Interface");
      Mesh_InterS.getGeometryObjects().setQuery(null);
      Mesh_InterS.getGeometryObjects().setObjects(StaticDomain_Inter);
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
      AutoMeshOperation RotationDomainMesh = mySim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.trimmer.TrimmerAutoMesher", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{Part_RotationDomain}));
      RotationDomainMesh.setPresentationName("02-Rotation Domain Mesh");
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
      integerValue_R.getQuantity().setDefinition("${Pnp}");
      PrismLayerStretching prismLayerStretching_R = RotationDomainMesh.getDefaultValues().get(PrismLayerStretching.class);
      prismLayerStretching_R.getStretchingQuantity().setDefinition("${Psp}");
      PrismThickness prismThickness_R = RotationDomainMesh.getDefaultValues().get(PrismThickness.class);
      prismThickness_R.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
      prismThickness_R.getAbsoluteSizeValue().setDefinition("${Ptp}");
      //prismThickness_R.getRelativeSizeScalar().setValue(50.0);
      PartsSimpleTemplateGrowthRate partsSimpleTemplateGrowthRate_R = RotationDomainMesh.getDefaultValues().get(PartsSimpleTemplateGrowthRate.class);
      partsSimpleTemplateGrowthRate_R.getGrowthRateOption().setSelected(PartsGrowthRateOption.Type.FAST);
      MaximumCellSize maximumCellSize_R = RotationDomainMesh.getDefaultValues().get(MaximumCellSize.class);
      maximumCellSize_R.getRelativeSizeScalar().setValue(100.0);
      // 02-Rotation Domain : Interface
      PartSurface RotationDomain_BladeK = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade-Key");
      PartSurface RotationDomain_Blade = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Propeller.Blade");
      PartSurface RotationDomain_Hub = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Propeller.Hub");
      PartSurface RotationDomain_Interface = Part_RotationDomain.getPartSurfaceManager().getPartSurface("Rotation Field.Interface");
      PartCurve RotationDomain_Edges = Part_RotationDomain.getPartCurveManager().getPartCurve("Propeller.Blade Edges");
      SurfaceCustomMeshControl Mesh_InterR = RotationDomainMesh.getCustomMeshControls().createSurfaceControl();
      Mesh_InterR.setPresentationName("Interface");
      Mesh_InterR.getGeometryObjects().setQuery(null);
      Mesh_InterR.getGeometryObjects().setObjects(RotationDomain_Interface);
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
      // 02-Rotation Domain : Blade
      SurfaceCustomMeshControl Mesh_Blade = RotationDomainMesh.getCustomMeshControls().createSurfaceControl();
      Mesh_Blade.setPresentationName("Blade");
      Mesh_Blade.getGeometryObjects().setQuery(null);
      Mesh_Blade.getGeometryObjects().setObjects(RotationDomain_BladeK, RotationDomain_Blade, RotationDomain_Hub);
      Mesh_Blade.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
      Mesh_Blade.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
      PartsTargetSurfaceSize MeshT_Blade = Mesh_Blade.getCustomValues().get(PartsTargetSurfaceSize.class);
      MeshT_Blade.getRelativeSizeScalar().setValue(25.0);
      PartsMinimumSurfaceSize MeshM_Blade = Mesh_Blade.getCustomValues().get(PartsMinimumSurfaceSize.class);
      MeshM_Blade.getRelativeSizeScalar().setValue(5.0);
      // 02-Rotation Domain : Blade Edges
      CurveCustomMeshControl Mesh_Edges = RotationDomainMesh.getCustomMeshControls().createCurveControl();
      Mesh_Edges.setPresentationName("Blade Edges");
      Mesh_Edges.getGeometryObjects().setQuery(null);
      Mesh_Edges.getGeometryObjects().setObjects(RotationDomain_Edges);
      Mesh_Edges.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
      Mesh_Edges.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
      PartsTargetSurfaceSize MeshT_Edges = Mesh_Edges.getCustomValues().get(PartsTargetSurfaceSize.class);
      MeshT_Edges.getRelativeSizeScalar().setValue(5.0);
      PartsMinimumSurfaceSize MehsM_Edges = Mesh_Edges.getCustomValues().get(PartsMinimumSurfaceSize.class);
      MehsM_Edges.getRelativeSizeScalar().setValue(1.0);
    }
  }

  public static class GenerateMesh extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      MeshPipelineController meshPipelineController_0 = mySim.get(MeshPipelineController.class);
      meshPipelineController_0.generateVolumeMesh();
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
      CartesianCoordinateSystem LCS_HP = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
      Region StaticDomain = mySim.getRegionManager().getRegion("01-Static Domain");
      Region RotationDomain = mySim.getRegionManager().getRegion("02-Rotation Domain");
      Boundary bdy_BladeK = RotationDomain.getBoundaryManager().getBoundary("Propeller.Blade-Key");
      Boundary bdy_Blade = RotationDomain.getBoundaryManager().getBoundary("Propeller.Blade");
      Boundary bdy_Hub = RotationDomain.getBoundaryManager().getBoundary("Propeller.Hub");
      // Mesh Section
      PlaneSection MidSection = (PlaneSection) mySim.getPartManager().createImplicitPart(
          new NeoObjectVector(new Object[]{StaticDomain, RotationDomain}), new DoubleVector(new double[]{1.0, 0.0, 0.0}),
          new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1,
          new DoubleVector(new double[]{0.0}));
      MidSection.setCoordinateSystem(LCS_HP);
      MidSection.setPresentationName("Mid Section");
      PlaneSection T1Section = (PlaneSection) mySim.getPartManager().createImplicitPart(
          new NeoObjectVector(new Object[]{StaticDomain, RotationDomain}), new DoubleVector(new double[]{0.0, 1.0, 0.0}),
          new DoubleVector(new double[]{0.0, 0.0, 0.0}), 0, 1,
          new DoubleVector(new double[]{0.0}));
      T1Section.setCoordinateSystem(LCS_HP);
      T1Section.setPresentationName("Longitudinal Section");
      // Create 02-Mesh Scene
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
      partDisplayer_2.getInputParts().setQuery(null);
      partDisplayer_2.getInputParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub, T1Section);
      scene_2.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
      scene_2.resetCamera();
      CurrentView currentView_2 = scene_2.getCurrentView();
      ViewAngle viewAngle_2 = currentView_2.getViewAngle();
      viewAngle_2.setValue(20.0);
    }
  }

  public static class CreateReport extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      Region RotationDomain = mySim.getRegionManager().getRegion("02-Rotation Domain");
      Boundary bdy_BladeK = RotationDomain.getBoundaryManager().getBoundary("Propeller.Blade-Key");
      Boundary bdy_Blade = RotationDomain.getBoundaryManager().getBoundary("Propeller.Blade");
      Boundary bdy_Hub = RotationDomain.getBoundaryManager().getBoundary("Propeller.Hub");
      LabCoordinateSystem GCS = mySim.getCoordinateSystemManager().getLabCoordinateSystem();
      CartesianCoordinateSystem LCS_H = ((CartesianCoordinateSystem) GCS.getLocalCoordinateSystemManager().getObject("Initial COS"));
      CartesianCoordinateSystem LCS_HP = ((CartesianCoordinateSystem) LCS_H.getLocalCoordinateSystemManager().getObject("Propeller"));
      // Wall Y Plus Report
      PrimitiveFieldFunction wallyplus = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("WallYplus"));
      AreaAverageReport WallYPlusReport = mySim.getReportManager().createReport(AreaAverageReport.class);
      WallYPlusReport.setPresentationName("Wall Y+");
      WallYPlusReport.setFieldFunction(wallyplus);
      WallYPlusReport.getParts().setObjects(bdy_BladeK, bdy_Blade);
      WallYPlusReport.createMonitor();
      // Thrust
      forceReport("Thrust", new Boundary[]{bdy_BladeK, bdy_Blade, bdy_Hub}, LCS_HP, null );
      forceReport("ThrustPressure", new Boundary[]{bdy_BladeK, bdy_Blade, bdy_Hub}, LCS_HP, TriBoolean.of(true));
      forceReport("ThrustShear", new Boundary[]{bdy_BladeK, bdy_Blade, bdy_Hub}, LCS_HP, TriBoolean.of(false));

      forceReport("Key.Thrust", new Boundary[]{bdy_BladeK}, LCS_HP, null );
      forceReport("Key.Pressure", new Boundary[]{bdy_BladeK}, LCS_HP, TriBoolean.of(true));
      forceReport("Key.Shear", new Boundary[]{bdy_BladeK}, LCS_HP, TriBoolean.of(false));
//      ForceReport ThrustReport = mySim.getReportManager().createReport(ForceReport.class);
//      ThrustReport.setPresentationName("Thrust");
//      ThrustReport.setCoordinateSystem(LCS_HP);
//      ThrustReport.getDirection().setComponents(-1.0, 0.0, 0.0);
//      ThrustReport.getParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub);
//      ThrustReport.createMonitor();
//      mySim.getAnnotationManager().createReportAnnotation(ThrustReport);
//      ForceReport ThrustPressureReport = mySim.getReportManager().createReport(ForceReport.class);
//      ThrustPressureReport.setPresentationName("ThrustPressure");
//      ThrustPressureReport.setCoordinateSystem(LCS_HP);
//      ThrustPressureReport.getDirection().setComponents(-1.0, 0.0, 0.0);
//      ThrustPressureReport.getForceOption().setSelected(ForceReportForceOption.Type.PRESSURE);
//      ThrustPressureReport.getParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub);
//      ThrustPressureReport.createMonitor();
//      ForceReport ThrustShearReport = mySim.getReportManager().createReport(ForceReport.class);
//      ThrustShearReport.setPresentationName("ThrustShear");
//      ThrustShearReport.setCoordinateSystem(LCS_HP);
//      ThrustShearReport.getDirection().setComponents(-1.0, 0.0, 0.0);
//      ThrustShearReport.getForceOption().setSelected(ForceReportForceOption.Type.SHEAR);
//      ThrustShearReport.getParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub);
//      ThrustShearReport.createMonitor();
      // Thrust-Key
//      ForceReport Thrust_Key = mySim.getReportManager().createReport(ForceReport.class);
//      Thrust_Key.setPresentationName("Key.Thrust");
//      Thrust_Key.setCoordinateSystem(LCS_HP);
//      Thrust_Key.getDirection().setComponents(-1.0, 0.0, 0.0);
//      Thrust_Key.getParts().setObjects(bdy_BladeK);
//      Thrust_Key.createMonitor();
//      ForceReport Tk_Pressure = mySim.getReportManager().createReport(ForceReport.class);
//      Tk_Pressure.setPresentationName("Key.Pressure");
//      Tk_Pressure.setCoordinateSystem(LCS_HP);
//      Tk_Pressure.getDirection().setComponents(-1.0, 0.0, 0.0);
//      Tk_Pressure.getForceOption().setSelected(ForceReportForceOption.Type.PRESSURE);
//      Tk_Pressure.getParts().setObjects(bdy_BladeK);
//      Tk_Pressure.createMonitor();
//      ForceReport Tk_Shear = mySim.getReportManager().createReport(ForceReport.class);
//      Tk_Shear.setPresentationName("Key.Shear");
//      Tk_Shear.setCoordinateSystem(LCS_HP);
//      Tk_Shear.getDirection().setComponents(-1.0, 0.0, 0.0);
//      Tk_Shear.getForceOption().setSelected(ForceReportForceOption.Type.SHEAR);
//      Tk_Shear.getParts().setObjects(bdy_BladeK);
//      Tk_Shear.createMonitor();
      // Torque
      MomentReport Torque = mySim.getReportManager().createReport(MomentReport.class);
      Torque.setPresentationName("Torque");
      Torque.setCoordinateSystem(LCS_HP);
      Torque.getDirection().setComponents(1.0, 0.0, 0.0);
      Torque.getParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub);
      Torque.createMonitor();
      mySim.getAnnotationManager().createReportAnnotation(Torque);
      // Torque-Key
      MomentReport Torque_Key = mySim.getReportManager().createReport(MomentReport.class);
      Torque_Key.setPresentationName("Key.Torque");
      Torque_Key.setCoordinateSystem(LCS_HP);
      Torque_Key.getDirection().setComponents(1.0, 0.0, 0.0);
      Torque_Key.getParts().setObjects(bdy_BladeK);
      Torque_Key.createMonitor();
      // KT, KQ, h0, J, n
      ExpressionReport KT = mySim.getReportManager().createReport(ExpressionReport.class);
      KT.setPresentationName("KT");
      KT.setDefinition("${ThrustReport}/997.561/pow($n, 2)/pow($Dp, 4)");
      KT.createMonitor();
      ExpressionReport KQ = mySim.getReportManager().createReport(ExpressionReport.class);
      KQ.setPresentationName("KQ");
      KQ.setDefinition("${TorqueReport}/997.561/pow($n, 2)/pow($Dp, 5)");
      KQ.createMonitor();
      ExpressionReport hO = mySim.getReportManager().createReport(ExpressionReport.class);
      hO.setPresentationName("hO");
      hO.setDefinition("${KT}/${KQ}*${J}/(2*${PI})");
      hO.createMonitor();
      ExpressionReport Report_J = mySim.getReportManager().createReport(ExpressionReport.class);
      Report_J.setPresentationName("Report-J");
      Report_J.setDefinition("$J");
      Report_J.createMonitor();
      ExpressionReport Report_n = mySim.getReportManager().createReport(ExpressionReport.class);
      Report_n.setPresentationName("Report-n");
      Report_n.setDefinition("$n");
      Report_n.createMonitor();
    }
    private void forceReport(String reportName, Boundary[] boundary, CoordinateSystem coordinatesystem, TriBoolean triboolean) {
      Simulation mySim = getActiveSimulation();
      ForceReport forceReport = mySim.getReportManager().createReport(ForceReport.class);
      forceReport.setPresentationName(reportName);
      forceReport.setCoordinateSystem(coordinatesystem);
      forceReport.getDirection().setComponents(-1.0, 0.0, 0.0);
      forceReport.getParts().setObjects(boundary);
      if (triboolean.toBoolean() == null){
        forceReport.getForceOption().setSelected(ForceReportForceOption.Type.PRESSURE_AND_SHEAR);
      } else if (triboolean.toBoolean()) {
        forceReport.getForceOption().setSelected(ForceReportForceOption.Type.PRESSURE);
      } else {
        forceReport.getForceOption().setSelected(ForceReportForceOption.Type.SHEAR);
      }
      forceReport.createMonitor();
    }
    private enum TriBoolean {
      TRUE, FALSE, NULL;
      public static TriBoolean of(Boolean value) {
        if (value == null) {
          return NULL;
        }
        return value ? TRUE : FALSE;
      }
      public Boolean toBoolean() {
        return switch (this) {
          case TRUE -> true;
          case FALSE -> false;
          default -> null;
        };
      }
    }
  }

  public static class CreateScene extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      Region StaticDomain = mySim.getRegionManager().getRegion("01-Static Domain");
      Region RotationDomain = mySim.getRegionManager().getRegion("02-Rotation Domain");
      Boundary bdy_BladeK = RotationDomain.getBoundaryManager().getBoundary("Propeller.Blade-Key");
      Boundary bdy_Blade = RotationDomain.getBoundaryManager().getBoundary("Propeller.Blade");
      Boundary bdy_Hub = RotationDomain.getBoundaryManager().getBoundary("Propeller.Hub");
      // Create Isosurface
      Units units_1 = mySim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 0, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
      QcriterionFunction qcriterionFunction_0 = ((QcriterionFunction) mySim.getFieldFunctionManager().getFunction("Qcriterion"));
      IsoPart IsoVortex = mySim.getPartManager().createIsoPart(new NeoObjectVector(new Object[]{StaticDomain, RotationDomain}), qcriterionFunction_0);
      IsoVortex.setMode(IsoMode.ISOVALUE_SINGLE);
      SingleIsoValue singleIsoValue_0 = IsoVortex.getSingleIsoValue();
      singleIsoValue_0.getValueQuantity().setValue(500.0);
      singleIsoValue_0.getValueQuantity().setUnits(units_1);
      IsoVortex.setPresentationName("IsoVortex");
      // Post-processing Tools
      LogoAnnotation starlogo = ((LogoAnnotation) mySim.getAnnotationManager().getObject("Logo"));
      PrimitiveFieldFunction wallyplus = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("WallYplus"));
      PrimitiveFieldFunction velocity = ((PrimitiveFieldFunction) mySim.getFieldFunctionManager().getFunction("Velocity"));
      VectorMagnitudeFieldFunction velocityM = ((VectorMagnitudeFieldFunction) velocity.getMagnitudeFunction());
      // Create 03-IsoVortex
      mySim.getSceneManager().createEmptyScene("Scene");
      Scene scene_3 = mySim.getSceneManager().getScene("Scene 1");
      scene_3.initializeAndWait();
      scene_3.setPresentationName("03-IsoVortex");
      PartDisplayer partDisplayer_3 = scene_3.getDisplayerManager().createPartDisplayer("Geometry", -1, 4);
      partDisplayer_3.initialize();
      partDisplayer_3.setPresentationName("Geometry");
      partDisplayer_3.setOutline(false);
      partDisplayer_3.setSurface(true);
      partDisplayer_3.getInputParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub);
      ScalarDisplayer scalarDisplayer_3 = scene_3.getDisplayerManager().createScalarDisplayer("Scalar");
      scalarDisplayer_3.initialize();
      scalarDisplayer_3.getInputParts().setObjects(IsoVortex);
      scalarDisplayer_3.getScalarDisplayQuantity().setFieldFunction(velocityM);
      scene_3.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
      scene_3.resetCamera();
      Legend legend_3 = scalarDisplayer_3.getLegend();
      legend_3.setLevels(32);
      legend_3.updateLayout(new DoubleVector(new double[]{0.25, 0.05}), 0.5, 0.04, 0);
      legend_3.setLabelFormat("%-#2.2f");
      legend_3.setNumberOfLabels(6);
      legend_3.setFontString("Times New Roman-Plain");
      legend_3.setShadow(false);
      CurrentView currentView_3 = scene_3.getCurrentView();
      ViewAngle viewAngle_3 = currentView_3.getViewAngle();
      viewAngle_3.setValue(30.0);
      scene_3.getAnnotationPropManager().removePropsForAnnotations(starlogo);
      // Create 04-Wall Y+
      mySim.getSceneManager().createEmptyScene("Scene");
      Scene scene_4 = mySim.getSceneManager().getScene("Scene 1");
      scene_4.initializeAndWait();
      scene_4.setPresentationName("04-Wall Y+");
      ScalarDisplayer scalarDisplayer_4 = scene_4.getDisplayerManager().createScalarDisplayer("Scalar");
      scalarDisplayer_4.initialize();
      scalarDisplayer_4.setFillMode(ScalarFillMode.NODE_FILLED);
      scalarDisplayer_4.getInputParts().setObjects(bdy_BladeK, bdy_Blade, bdy_Hub);
      scalarDisplayer_4.getScalarDisplayQuantity().setFieldFunction(wallyplus);
      Legend legend_4 = scalarDisplayer_4.getLegend();
      legend_4.updateLayout(new DoubleVector(new double[]{0.25, 0.05}), 0.5, 0.03, 0);
      legend_4.setLabelFormat("%-#3.3f");
      legend_4.setNumberOfLabels(6);
      legend_4.setFontString("Times New Roman-Plain");
      legend_4.setShadow(false);
      scene_4.setViewOrientation(new DoubleVector(new double[]{0.0, -1.0, 0.0}), new DoubleVector(new double[]{0.0, 0.0, 1.0}));
      scene_4.resetCamera();
      CurrentView currentView_4 = scene_4.getCurrentView();
      ViewAngle viewAngle_4 = currentView_4.getViewAngle();
      viewAngle_4.setValue(30.0);
      scene_4.getAnnotationPropManager().removePropsForAnnotations(starlogo);
    }
  }

  public static class SetupSolver extends StarMacro {
    @Override
    public void execute() {
      Simulation mySim = getActiveSimulation();
      ImplicitUnsteadySolver implicitUnsteadySolver_0 = mySim.getSolverManager().getSolver(ImplicitUnsteadySolver.class);
      implicitUnsteadySolver_0.getTimeStep().setDefinition("floor(2E5/360/${n})/1E5");
      InnerIterationStoppingCriterion innerIterationStoppingCriterion_0 = ((InnerIterationStoppingCriterion) mySim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Inner Iterations"));
      innerIterationStoppingCriterion_0.setMaximumNumberInnerIterations(5);
      PhysicalTimeStoppingCriterion physicalTimeStoppingCriterion_0 = ((PhysicalTimeStoppingCriterion) mySim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Physical Time"));
      physicalTimeStoppingCriterion_0.getMaximumTime().setValue(2.0);
      StepStoppingCriterion stepStoppingCriterion_0 = ((StepStoppingCriterion) mySim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps"));
      stepStoppingCriterion_0.setIsUsed(false);
    }
  }
  @SuppressWarnings("ResultOfMethodCallIgnored")
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
      deltaTimeUpdateFrequency_0.setDeltaTime("0.5", units_s);
      // Save
      Date d = new Date();
      DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmm");
      String s = sdf3.format(d);
      String work1FileName = "Open Water Test";
      File dir = new File(resolveWorkPath() + File.separator + work1FileName);
      if (dir.exists()) {
        mySim.println(dir.getAbsolutePath() + " already exists");
      } else {
        dir.mkdir();
        mySim.println(dir.getAbsolutePath() + " created successfully");
      }
      mySim.saveState(resolvePath(work1FileName + File.separator + "CASE_OPENWATERTEST_J=0.0_" + s + ".sim"));
    }
  }
  @SuppressWarnings("SameParameterValue")
  public static class CircularSave extends StarMacro {
    @Override
    public void execute() {
      for ( double i : Param_J ) {
        ChangeParam("J", i);
        SaveCase(caseName, i);
      }
    }
    @SuppressWarnings("SameParameterValue")
    private void ChangeParam(String paramName, double param) {
      Simulation mySim = getActiveSimulation();
      ScalarGlobalParameter scalarGlobalParameter_0 = ((ScalarGlobalParameter) mySim.get(GlobalParameterManager.class).getObject(paramName));
      scalarGlobalParameter_0.getQuantity().setValue(param);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void SaveCase(String caseName, double param) {
      Simulation mySim = getActiveSimulation();
      String work1FileName = "Open Water Test";
      File dir = new File(resolveWorkPath() + File.separator + work1FileName);
      if (dir.exists()) {
        mySim.println(dir.getAbsolutePath() + " already exists");
      } else {
        dir.mkdir();
        mySim.println(dir.getAbsolutePath() + " created successfully");
      }
      mySim.saveState(resolvePath(work1FileName + File.separator + caseName + "_OPENWATERTEST_J=" + param + ".sim"));
    }
  }

}
