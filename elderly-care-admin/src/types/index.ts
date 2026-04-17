// User and Authentication Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export interface UserInfo {
  userId: number;
  username: string;
  realName: string;
  phone?: string;
  email?: string;
  gender?: string;
  avatar?: string;
  userType?: string;
  tenantId?: string;
  roles: string[];
  permissions: string[];
  status?: number;
  createTime?: string;
}

// Provider Types
export interface Provider {
  providerId: string;
  providerName: string;
  providerType: string;
  creditCode: string;
  legalPerson: string;
  contactPhone: string;
  address: string;
  serviceAreas: string;
  description: string;
  auditStatus: string;
  auditComment: string;
  auditTime: string;
  auditorId: number;
  status: string;
  rating: number;
  ratingCount: number;
  createTime: string;
  updateTime: string;
}

export interface ProviderQuery {
  name?: string;
  code?: string;
  status?: string;
  level?: number;
  regionCode?: string;
  page: number;
  pageSize: number;
}

// Staff Types
export interface Staff {
  staffId: string;
  staffNo: string;
  staffName: string;
  providerId: string;
  providerName: string;
  gender: number;
  idCard: string;
  phone: string;
  birthDate: string;
  nation: string;
  education: string;
  politicalStatus: string;
  maritalStatus: string;
  domicileAddress: string;
  residenceAddress: string;
  emergencyContact: string;
  emergencyPhone: string;
  serviceTypes: string[];
  serviceAreas: string[];
  workStatus: string;
  entryDate: string;
  leaveDate: string;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface StaffQuery {
  name?: string;
  phone?: string;
  providerId?: string;
  serviceType?: string;
  status?: number;
  page: number;
  pageSize: number;
}

// Elder Types
export interface Elder {
  elderId: string;
  name: string;
  gender: string;
  birthDate: string;
  age: number;
  idCard: string;
  phone: string;
  ethnicity: string;
  education: string;
  maritalStatus: string;
  politicalStatus: string;
  photoUrl: string;
  status: string;
  registerDate: string;
  address: string;
  areaId: string;
  careLevel: string;
  careType: string;
  emergencyContact: string;
  emergencyPhone: string;
  subsidyType: string;
  subsidyAmount: number;
  providerId?: string;
  providerName?: string;
  createTime: string;
  updateTime: string;
}

export interface ElderQuery {
  name?: string;
  idCard?: string;
  careLevel?: number;
  subsidyType?: number;
  status?: number;
  regionCode?: string;
  page: number;
  pageSize: number;
}

// Order Types
export interface Order {
  id: string;
  orderNo: string;
  elderId: string;
  elderName: string;
  elderPhone: string;
  elderAddress: string;
  providerId: string;
  providerName: string;
  staffId?: string;
  staffName?: string;
  serviceType: string;
  serviceContent: string;
  serviceTime: string;
  serviceDuration: number;
  serviceFee: number;
  subsidyFee: number;
  selfPayFee: number;
  status: string;
  statusCode: number;
  statusName: string;
  remark?: string;
  createTime: string;
  updateTime: string;
}

export interface OrderQuery {
  orderNo?: string;
  elderName?: string;
  providerId?: string;
  staffId?: string;
  serviceType?: string;
  status?: number;
  startDate?: string;
  endDate?: string;
  page: number;
  pageSize: number;
}

// Financial Types
export interface Financial {
  id: string;
  settlementNo: string;
  orderId: string;
  orderNo: string;
  providerId: string;
  providerName: string;
  elderName: string;
  serviceType: string;
  serviceFee: number;
  subsidyFee: number;
  selfPayFee: number;
  settlementAmount: number;
  settlementStatus: number;
  settlementTime?: string;
  paymentTime?: string;
  paymentMethod?: string;
  createTime: string;
  updateTime: string;
}

export interface FinancialQuery {
  settlementNo?: string;
  providerId?: string;
  elderName?: string;
  settlementStatus?: number;
  startDate?: string;
  endDate?: string;
  page: number;
  pageSize: number;
}

// Evaluation Types
export interface Evaluation {
  id: string;
  orderId: string;
  orderNo: string;
  elderId: string;
  elderName: string;
  providerId: string;
  providerName: string;
  staffId: string;
  staffName: string;
  serviceType: string;
  serviceScore: number;
  attitudeScore: number;
  skillScore: number;
  punctualityScore: number;
  overallScore: number;
  content?: string;
  images?: string[];
  reply?: string;
  replyTime?: string;
  createTime: string;
}

export interface EvaluationQuery {
  orderNo?: string;
  elderName?: string;
  providerId?: string;
  staffId?: string;
  serviceType?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  pageSize: number;
}

// Config Types
export interface Config {
  id: string;
  category: string;
  code: string;
  name: string;
  value: string;
  sort: number;
  status: number;
  remark?: string;
  createTime: string;
  updateTime: string;
}

// Statistics Types
export interface DashboardStats {
  totalProviders: number;
  activeProviders: number;
  totalStaff: number;
  activeStaff: number;
  totalElders: number;
  activeElders: number;
  totalOrders: number;
  completedOrders: number;
  pendingOrders: number;
  totalRevenue: number;
  totalSubsidy: number;
  totalSelfPay: number;
  todayOrders: number;
  todayRevenue: number;
  monthOrders: number;
  monthRevenue: number;
}

export interface StatisticsQuery {
  startDate?: string;
  endDate?: string;
  regionCode?: string;
  providerId?: number;
}

export interface ChartData {
  date?: string;
  value: number;
  label?: string;
}

// System Types
export interface Role {
  id: number;
  name: string;
  code: string;
  description?: string;
  permissions: string[];
  status: number;
  createTime: string;
  updateTime: string;
}

export interface Permission {
  id: number;
  name: string;
  code: string;
  type: number;
  parentId?: number;
  path?: string;
  icon?: string;
  sort: number;
  status: number;
}

// API Response Types
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ServiceLog Types
export interface ServiceLog {
  id: string;
  logNo: string;
  orderId: string;
  orderNo: string;
  elderId: string;
  elderName: string;
  elderPhone: string;
  elderAddress: string;
  staffId: string;
  staffName: string;
  staffPhone: string;
  providerId: string;
  providerName: string;
  serviceTypeCode: string;
  serviceTypeName: string;
  serviceDate: string;
  serviceStartTime: string;
  serviceEndTime: string;
  serviceDuration: number;
  serviceStatus: string;
  actualDuration: number;
  serviceScore: number;
  serviceComment: string;
  servicePhotos: string;
  elderSignature: string;
  anomalyType: string;
  anomalyDesc: string;
  anomalyPhotos: string;
  anomalyStatus: string;
  auditStatus: string;
  createTime: string;
  updateTime: string;
}

export interface ServiceLogQuery {
  orderNo?: string;
  elderName?: string;
  staffName?: string;
  serviceStatus?: string;
  auditStatus?: string;
  page: number;
  pageSize: number;
}

// QualityCheck Types
export interface QualityCheck {
  id: string;
  checkNo: string;
  orderId: string;
  orderNo: string;
  serviceLogId: string;
  serviceCategory: string;
  providerId: string;
  providerName: string;
  staffId: string;
  staffName: string;
  checkType: string;
  checkMethod: string;
  checkScore: number;
  checkResult: string;
  checkPhotos: string;
  checkRemark: string;
  checkTime: string;
  checkerId: string;
  checkerName: string;
  needRectify: boolean;
  rectifyNotice: string;
  rectifyDeadline: string;
  rectifyStatus: string;
  rectifyPhotos: string;
  rectifyRemark: string;
  recheckTime: string;
  recheckResult: string;
  createTime: string;
  updateTime: string;
}

export interface QualityCheckQuery {
  checkNo?: string;
  orderNo?: string;
  providerName?: string;
  staffName?: string;
  checkResult?: string;
  rectifyStatus?: string;
  page: number;
  pageSize: number;
}

// Menu Types
export interface MenuItem {
  key: string;
  label: string;
  icon?: React.ReactNode;
  children?: MenuItem[];
}
