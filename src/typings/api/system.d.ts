/**
 * 系统管理模块 - 字典、参数、日志等
 */

declare namespace Api {
  namespace System {
    /** 字典类型 */
    interface DictType {
      /** 字典类型ID */
      id: string;
      /** 字典类型编码 */
      dictCode: string;
      /** 字典类型名称 */
      dictName: string;
      /** 字典类型描述 */
      dictDesc?: string;
      /** 是否系统字典 */
      isSystem?: boolean;
      /** 状态 */
      status: string;
      /** 排序 */
      sortOrder?: number;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 字典类型表单 */
    interface DictTypeForm {
      /** 字典类型ID（编辑时需要） */
      id?: string;
      /** 字典类型编码 */
      dictCode: string;
      /** 字典类型名称 */
      dictName: string;
      /** 字典类型描述 */
      dictDesc?: string;
      /** 状态 */
      status: string;
      /** 排序 */
      sortOrder?: number;
    }

    /** 字典项 */
    interface DictItem {
      /** 字典项ID */
      id: string;
      /** 字典类型ID */
      dictTypeId: string;
      /** 字典项编码 */
      itemCode: string;
      /** 字典项名称 */
      itemName: string;
      /** 字典项描述 */
      itemDesc?: string;
      /** 排序 */
      sortOrder?: number;
      /** 状态 */
      status: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 字典项表单 */
    interface DictItemForm {
      /** 字典项ID（编辑时需要） */
      id?: string;
      /** 字典类型ID */
      dictTypeId: string;
      /** 字典项编码 */
      itemCode: string;
      /** 字典项名称 */
      itemName: string;
      /** 字典项描述 */
      itemDesc?: string;
      /** 排序 */
      sortOrder?: number;
      /** 状态 */
      status: string;
    }

    /** 系统参数 */
    interface SystemParam {
      /** 参数ID */
      id: string;
      /** 参数分类 */
      paramCategory: string;
      /** 参数编码 */
      paramCode: string;
      /** 参数名称 */
      paramName: string;
      /** 参数值 */
      paramValue: string;
      /** 参数类型 */
      paramType: string;
      /** 默认值 */
      defaultValue?: string;
      /** 参数说明 */
      paramDesc?: string;
      /** 是否系统参数 */
      isSystem?: boolean;
      /** 状态 */
      status: string;
      /** 排序 */
      sortOrder?: number;
    }

    /** 登录日志 */
    interface LoginLog {
      /** 日志ID */
      id: string;
      /** 用户ID */
      userId: string;
      /** 用户名 */
      userName: string;
      /** 登录时间 */
      loginTime: string;
      /** 登录IP */
      loginIp: string;
      /** 登录地点 */
      loginLocation?: string;
      /** 登录设备 */
      loginDevice?: string;
      /** 登录状态 */
      loginStatus: string;
      /** 失败原因 */
      failReason?: string;
    }

    /** 操作日志 */
    interface OperationLog {
      /** 日志ID */
      id: string;
      /** 用户ID */
      userId: string;
      /** 用户名 */
      userName: string;
      /** 操作模块 */
      operationModule: string;
      /** 操作类型 */
      operationType: string;
      /** 操作内容 */
      operationContent?: string;
      /** 操作时间 */
      operationTime: string;
      /** IP地址 */
      ipAddress?: string;
      /** 请求方法 */
      requestMethod?: string;
      /** 请求URL */
      requestUrl?: string;
      /** 请求参数 */
      requestParams?: string;
      /** 响应结果 */
      responseResult?: string;
    }
  }
}
