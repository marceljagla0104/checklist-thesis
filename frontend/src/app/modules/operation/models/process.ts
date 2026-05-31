import {
  ElementDTO,
  OperationDTO,
  SubprocessDTO,
} from '../services/api/operation';
import { Role } from '../../shared/models';

export class Operation {
  id: string;
  heading: string;
  subprocesses: Subprocess[];

  constructor(operationDTO: OperationDTO) {
    this.id = operationDTO.id;
    this.heading = operationDTO.name;
    this.subprocesses = operationDTO.subprocesses.map((s) => new Subprocess(s));
  }
}

export class Subprocess {
  public name: string;
  public elements: Element[];

  constructor(subprocessDTO: SubprocessDTO) {
    this.name = subprocessDTO.name;
    this.elements = subprocessDTO.elements.map((e) => Element.fromDTO(e));
  }

  getAllChildrenIds(elementId: string): string[] {
    let childrenIds: string[] = [];
    let element = this.elements.find((e) => e.id === elementId);
    if (element.children.length > 0) {
      element.children.forEach((c) => {
        childrenIds.push(c.id);
        childrenIds = childrenIds.concat(this.getAllChildrenIds(c.id));
      });
    }
    return childrenIds;
  }
}

export class Element {
  public id: string;
  public type: Type;
  public inputType: InputType;
  public roles: Role[];
  public name: string;
  public surgeonInfo: string;
  public surgeonImages: Image[];
  public studentInfo: string;
  public studentImages: Image[];
  public instruments: string[];
  public eventIds: string[];
  public children: Child[];
  public pathIds: string[];
  public hidden: boolean;
  public allChildIds: string[];
  public circulatingTriggerId;

  constructor(
    id: string,
    type: Type,
    inputType: InputType,
    roles: Role[],
    name: string,
    surgeonInfo: string,
    surgeonImages: Image[],
    studentInfo: string,
    studentImages: Image[],
    instruments: string[],
    eventIds: string[],
    children: Child[],
    pathIds: string[],
    hidden: boolean,
    allChildIds: string[],
    circulatingTriggerId: string,
  ) {
    this.id = id;
    this.type = type;
    this.inputType = inputType;
    this.roles = roles;
    this.name = name;
    this.surgeonInfo = surgeonInfo;
    this.surgeonImages = surgeonImages;
    this.studentInfo = studentInfo;
    this.studentImages = studentImages;
    this.instruments = instruments;
    this.eventIds = eventIds;
    this.children = children;
    this.pathIds = pathIds;
    this.hidden = hidden;
    this.allChildIds = allChildIds;
    this.circulatingTriggerId = circulatingTriggerId;
  }

  public static fromDTO(elementDTO: ElementDTO): Element {
    return new Element(
      elementDTO.id,
      Type[elementDTO.type as keyof typeof Type],
      InputType[elementDTO.inputType as keyof typeof InputType],
      elementDTO.roles.map((r) => Role[r as keyof typeof Role]),
      elementDTO.name,
      elementDTO.surgeonInfo,
      elementDTO.surgeonImages.map((i) =>
        Image.fromObject({ id: i.id, url: i.url, caption: i.caption }),
      ),
      elementDTO.studentInfo,
      elementDTO.studentImages.map((i) =>
        Image.fromObject({ id: i.id, url: i.url, caption: i.caption }),
      ),
      elementDTO.instruments,
      elementDTO.eventIds,
      elementDTO.children.map((c) =>
        Child.fromObject({ id: c.id, pathDescription: c.pathDescription }),
      ),
      elementDTO.pathIds,
      false, // todo ???
      elementDTO.allChildIds,
      elementDTO.circulatingTriggerId,
    );
  }
}

export enum Type {
  TASK = 'TASK',
  XOR = 'XOR',
  PARALLEL = 'PARALLEL',
  DATA = 'DATA',
  TEXT = 'TEXT',
  INPUT = 'INPUT',
  END_EVENT = 'END_EVENT',
  START_EVENT = 'START_EVENT',
  BOUNDARY_EVENT = 'BOUNDARY_EVENT',
}

export enum InputType {
  TEXT = 'TEXT',
  UPLOAD = 'UPLOAD',
  PICTURE = 'PICTURE',
  CHECKBOX = 'CHECKBOX',
  HEADING = 'HEADING',
  NONE = 'NONE',
}

export class Image {
  public id: string;
  public url: string;
  public caption: string;

  public static fromObject(obj: Partial<Image>): Image {
    if (!obj) {
      return null;
    }
    const data = new Image();
    Object.assign(data, obj);
    return data;
  }
}

export class Child {
  public id: string;
  public pathDescription: string;

  public static fromObject(obj: Partial<Child>): Child {
    if (!obj) {
      return null;
    }
    const child = new Child();
    Object.assign(child, obj);
    return child;
  }
}
